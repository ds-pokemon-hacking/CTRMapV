package ctrmap.formats.pokemon.gen5.sequence;

import xstandard.formats.yaml.Yaml;
import xstandard.formats.yaml.YamlNode;
import xstandard.formats.yaml.YamlReflectUtil;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ctrmap.formats.pokemon.gen5.sequence.commands.SeqCommandObject;
import ctrmap.formats.pokemon.gen5.sequence.commands.SeqCommandObjectFactory;
import ctrmap.formats.pokemon.gen5.sequence.rel.RelocatableSequence;

/**
 *
 */
public class Sequence {
	
	public int seqId;
	
	public SeqParameters params;

	public List<SeqCommandObject> playbackCommands = new ArrayList<>();
	public List<SeqCommandObject> endCommands = new ArrayList<>();
	
	public SeqResources resources;
	
	public Sequence(){
		
	}

	public Sequence(DataIOStream io, SeqHeader header) throws IOException {
		read(io, header);
	}
	
	public Sequence(Yaml yml){
		seqId = yml.getRootNodeKeyValueInt(SeqYmlKeys.SEQID);
		
		params = YamlReflectUtil.deserialize(yml.getRootNodeKeyNode("Parameters"), SeqParameters.class);
		
		YamlNode pbCommands = yml.getRootNodeKeyNode(SeqYmlKeys.CMDS1);
		for (YamlNode pbc : pbCommands.children){
			playbackCommands.add(SeqCommandObjectFactory.createSeqCommandObject(pbc));
		}
		
		YamlNode eCommands = yml.getRootNodeKeyNode(SeqYmlKeys.CMDS2);
		for (YamlNode ec : eCommands.children){
			endCommands.add(SeqCommandObjectFactory.createSeqCommandObject(ec));
		}
		
		resources = new SeqResources(yml.getRootNodeKeyNode(SeqYmlKeys.RESOURCES));
	}
	
	public RelocatableSequence toRelocatable(){
		return new RelocatableSequence(this);
	}
	
	protected final void read(DataIOStream io, SeqHeader header) throws IOException {
		seqId = header.seqId;
		
		io.seek(header.paramsPtr);
		params = new SeqParameters(io);
		
		io.seek(header.commandsPtr);
		readCmdList(playbackCommands, io);
		
		io.seek(header.endCommandsPtr);
		readCmdList(endCommands, io);
		
		resources = new SeqResources(io, header);
	}
	
	public Yaml getYML(){
		Yaml yml = new Yaml();
		yml.getEnsureRootNodeKeyNode(SeqYmlKeys.SEQID).setValueInt(seqId);
		
		yml.root.addChild(params.getYMLNode());
		
		YamlNode pbCommands = yml.getEnsureRootNodeKeyNode(SeqYmlKeys.CMDS1);
		for (SeqCommandObject sc : playbackCommands){
			pbCommands.addChild(SeqCommandObjectFactory.createYmlNode(sc));
		}
		
		YamlNode eCommands = yml.getEnsureRootNodeKeyNode(SeqYmlKeys.CMDS2);
		for (SeqCommandObject sc : endCommands){
			eCommands.addChild(SeqCommandObjectFactory.createYmlNode(sc));
		}
		
		yml.root.addChild(resources.getYMLNode());
		return yml;
	}

	private static void readCmdList(List<SeqCommandObject> list, DataInput in) throws IOException {
		while (true) {
			SeqCmd cmd = new SeqCmd(in);
			list.add(cmd.createCommandObject());
			if (cmd.opCode == SeqOpCode.CMD_END) {
				break;
			}
		}
	}
}

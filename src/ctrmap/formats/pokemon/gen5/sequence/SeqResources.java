package ctrmap.formats.pokemon.gen5.sequence;

import xstandard.formats.yaml.Key;
import xstandard.formats.yaml.YamlListElement;
import xstandard.formats.yaml.YamlNode;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ctrmap.formats.pokemon.gen5.sequence.rel.RelocatableSequence;
import ctrmap.formats.pokemon.gen5.sequence.rel.RelocatableWriter;

public class SeqResources {

	public static final boolean SEQRES_WORDALIGN = true;

	public List<ResourceConfig> resources = new ArrayList<>();

	public SeqResources(DataIOStream in, SeqHeader hdr) throws IOException {
		for (int i = 0; i < hdr.resourcesCount; i++) {
			in.seek(hdr.resourcesPtr + i * ResourceConfig.BYTES);
			resources.add(new ResourceConfig(i, in));
		}
	}
	
	public SeqResources(YamlNode node){
		for (YamlNode child : node.children){
			resources.add(new ResourceConfig(child));
		}
	}

	public void write(RelocatableWriter out) throws IOException {
		List<RelocatableWriter.RelocatableOffset> bundlePtrs = new ArrayList<>();
		for (ResourceConfig cfg : resources) {
			bundlePtrs.add(cfg.write(out));
		}

		for (int i = 0; i < resources.size(); i++) {
			ResourceConfig cfg = resources.get(i);
			out.pad(RelocatableSequence.RS_PADDING);
			out.writeStringUnterminated("BND0");
			bundlePtrs.get(i).setHere();

			List<RelocatableWriter.RelocatableOffset> groupPtrs = new ArrayList<>();
			for (DNCResourceBundle bnd : cfg.resourceBundles) {
				groupPtrs.add(bnd.write(out));
				if (SEQRES_WORDALIGN) {
					out.pad(4);
				}
			}

			for (int j = 0; j < cfg.resourceBundles.size(); j++) {
				DNCResourceBundle bnd = cfg.resourceBundles.get(j);
				out.writeStringUnterminated("GRP0");
				groupPtrs.get(j).setHere();
				List<RelocatableWriter.RelocatableOffset> dataIDPtrs = new ArrayList<>();
				for (ResourceAnimationGroup grp : bnd.resAnimGroups) {
					dataIDPtrs.add(grp.write(out));
					if (SEQRES_WORDALIGN) {
						out.pad(4);
					}
				}

				for (int k = 0; k < bnd.resAnimGroups.size(); k++) {
					ResourceAnimationGroup grp = bnd.resAnimGroups.get(k);
					out.writeStringUnterminated("DAT0");
					dataIDPtrs.get(k).setHere();
					for (int datID : grp.dataIDs) {
						out.writeShort(datID);
					}
				}
				if (SEQRES_WORDALIGN) {
					out.pad(4);
				}
			}
		}
	}
	
	public YamlNode getYMLNode(){
		YamlNode n = new YamlNode(new Key(SeqYmlKeys.RESOURCES));
		
		for (ResourceConfig cfg : resources){
			n.addChild(cfg.getYMLNode());
		}
		
		return n;
	}

	public static class ResourceConfig {

		public static final int BYTES = 8;

		public int id;
		public List<DNCResourceBundle> resourceBundles = new ArrayList<>();
		public ResourceBundleDecisionType bundleSelectionType;

		public ResourceConfig(int index, DataIOStream in) throws IOException {
			id = index;
			int resBundlesPtr = in.readInt();
			int resBundlesCount = in.readUnsignedShort();
			bundleSelectionType = ResourceBundleDecisionType.values()[in.readUnsignedShort()];

			for (int i = 0; i < resBundlesCount; i++) {
				in.seek(resBundlesPtr + i * DNCResourceBundle.BYTES);
				resourceBundles.add(new DNCResourceBundle(i, in));
			}
		}

		public ResourceConfig(YamlNode n){
			id = n.getChildIntValue(SeqYmlKeys.RSC_ID_COMMON);
			bundleSelectionType = ResourceBundleDecisionType.valueOf(n.getChildValue(SeqYmlKeys.RSC_BUNDLE_DECISION));
			
			for (YamlNode bnd : n.getChildByName(SeqYmlKeys.RSC_BUNDLES).children){
				resourceBundles.add(new DNCResourceBundle(bnd));
			}
		}
		
		public RelocatableWriter.RelocatableOffset write(RelocatableWriter out) throws IOException {
			RelocatableWriter.RelocatableOffset bundlesPtr = new RelocatableWriter.RelocatableOffset(out);
			out.writeShort(resourceBundles.size());
			out.writeShort(bundleSelectionType.ordinal());
			return bundlesPtr;
		}
		
		public YamlNode getYMLNode(){
			YamlNode n = new YamlNode(new YamlListElement());
			
			n.addChild(SeqYmlKeys.RSC_ID_COMMON, new YamlNode(String.valueOf(id)));
			n.addChild(SeqYmlKeys.RSC_BUNDLE_DECISION, bundleSelectionType.name());
			
			YamlNode bundles = n.getEnsureChildByName(SeqYmlKeys.RSC_BUNDLES);
			for (DNCResourceBundle bnd : resourceBundles){
				bundles.addChild(bnd.getYMLNode());
			}
			
			return n;
		}

		public static enum ResourceBundleDecisionType {
			NONE,
			PLAYER_GENDER,
			USER_PARAM,
			SEASON
		}
	}

	public static class DNCResourceBundle {

		public static final int BYTES = 8;

		public int id;
		public List<ResourceAnimationGroup> resAnimGroups = new ArrayList<>();

		public DNCResourceBundle(int idx, DataIOStream in) throws IOException {
			id = idx;
			int animGroupsPtr = in.readInt();
			int animGroupsCount = in.readInt();

			for (int i = 0; i < animGroupsCount; i++) {
				in.seek(animGroupsPtr + i * ResourceAnimationGroup.BYTES);
				resAnimGroups.add(new ResourceAnimationGroup(i, in));
			}
		}
		
		public DNCResourceBundle(YamlNode n){
			id = n.getChildIntValue(SeqYmlKeys.RSC_ID_COMMON);
			
			for (YamlNode grp : n.getChildByName(SeqYmlKeys.RSC_ANIM_GROUPS).children){
				resAnimGroups.add(new ResourceAnimationGroup(grp));
			}
		}

		public RelocatableWriter.RelocatableOffset write(RelocatableWriter out) throws IOException {
			RelocatableWriter.RelocatableOffset animGroupsPtr = new RelocatableWriter.RelocatableOffset(out);
			out.writeInt(resAnimGroups.size());
			return animGroupsPtr;
		}
		
		public YamlNode getYMLNode(){
			YamlNode n = new YamlNode(new YamlListElement());
			
			n.addChild(SeqYmlKeys.RSC_ID_COMMON, new YamlNode(String.valueOf(id)));
			
			YamlNode animGroups = n.getEnsureChildByName(SeqYmlKeys.RSC_ANIM_GROUPS);
			for (ResourceAnimationGroup grp : resAnimGroups){
				animGroups.addChild(grp.getYMLNode());
			}
			
			return n;
		}
	}

	public static class ResourceAnimationGroup {

		public static final int BYTES = 8;

		public int id;
		public List<Integer> dataIDs = new ArrayList<>();

		public ResourceAnimationGroup(int idx, DataIOStream in) throws IOException {
			id = idx;
			int dataIDsPtr = in.readInt();
			int dataIDsCount = in.readInt();

			in.seek(dataIDsPtr);
			for (int i = 0; i < dataIDsCount; i++) {
				dataIDs.add(in.readUnsignedShort());
			}
		}
		
		public ResourceAnimationGroup(YamlNode n){
			id = n.getChildIntValue(SeqYmlKeys.RSC_ID_COMMON);
			
			dataIDs = n.getChildByName(SeqYmlKeys.RSC_DATA_IDS).getChildValuesAsListInt();
		}

		public RelocatableWriter.RelocatableOffset write(RelocatableWriter writer) throws IOException {
			RelocatableWriter.RelocatableOffset dataIDsPtr = new RelocatableWriter.RelocatableOffset(writer);
			writer.writeInt(dataIDs.size());
			return dataIDsPtr;
		}
		
		public YamlNode getYMLNode(){
			YamlNode n = new YamlNode(new YamlListElement());
			
			n.addChild(SeqYmlKeys.RSC_ID_COMMON, new YamlNode(String.valueOf(id)));
			
			YamlNode dataIDsNode = n.getEnsureChildByName(SeqYmlKeys.RSC_DATA_IDS);
			for (Integer datId : dataIDs){
				YamlNode datIdNode = new YamlNode(new YamlListElement());
				datIdNode.addChild(new YamlNode(String.valueOf(datId)));
				dataIDsNode.addChild(datIdNode);
			}
			
			return n;
		}
	}
}

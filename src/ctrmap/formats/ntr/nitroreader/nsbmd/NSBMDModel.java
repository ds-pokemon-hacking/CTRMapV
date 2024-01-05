package ctrmap.formats.ntr.nitroreader.nsbmd;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.common.gfx.commands.mtx.MtxMode;
import ctrmap.formats.ntr.nitroreader.common.NNSPatriciaTreeReader;
import ctrmap.formats.ntr.nitroreader.nsbmd.sbccommands.*;
import java.util.ArrayList;
import java.util.List;
import ctrmap.formats.ntr.nitroreader.common.Utils;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.MeshVisibilityGroup;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.util.MeshProcessor;
import xstandard.INamed;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.joml.Matrix3f;
import org.joml.Matrix4x3f;
import xstandard.math.BitMath;

public class NSBMDModel implements INamed {

	public String name;

	public List<SBCCommand> commands;
	public final NSBMDModelHeader modelHeader;
	public List<NSBMDMaterial> materials;
	public List<NSBMDJoint> joints;
	public List<NSBMDMesh> meshes;

	public List<Matrix4x3f> invBindPoseMatrices = new ArrayList<>();
	private List<Matrix3f> invNormalMatrices = new ArrayList<>();

	public NSBMDModel(NTRDataIOStream data, String name) throws IOException {
		data.setBaseHere();
		this.name = name;
		int size = data.readInt();
		int rendererCodeOffs = data.readInt();
		int materialsOffs = data.readInt();
		int meshesOffs = data.readInt();
		int invBindPoseOffs = data.readInt();
		modelHeader = new NSBMDModelHeader(data);
		readSkeleton(data);
		data.seek(materialsOffs);
		readMaterials(data);
		data.seek(meshesOffs);
		readMeshes(data);
		data.seek(rendererCodeOffs);
		readSBC(data, materialsOffs);

		boolean needsInvBind = false;
		for (SBCCommand sbcc : commands) {
			if (sbcc instanceof NODEMIX) {
				needsInvBind = true;
				break;
			}
		}
		if (needsInvBind) {
			data.seek(invBindPoseOffs);
			for (int i = 0; i < joints.size(); i++) {
				invBindPoseMatrices.add(new Matrix4x3f().set(data.readFX32Array(4 * 3)));
				invNormalMatrices.add(new Matrix3f().set(data.readFX32Array(3 * 3)));
			}
		}

		data.resetBase();
	}

	private void readSkeleton(NTRDataIOStream data) throws IOException {
		data.setBaseHere();
		joints = NNSPatriciaTreeReader.processOffsetTree(data, (io, entry) -> {
			return new NSBMDJoint(data, entry.getName());
		});
		data.resetBase();
	}

	private void readMaterials(NTRDataIOStream data) throws IOException {
		data.setBaseHere();

		int textureNameMapOffs = data.readUnsignedShort();
		int paletteNameMapOffs = data.readUnsignedShort();
		
		materials = NNSPatriciaTreeReader.processOffsetTree(data, (io, entry) -> {
			return new NSBMDMaterial(data, entry.getName());
		});
		
		data.seek(textureNameMapOffs);
		NNSPatriciaTreeReader.Entry[] textureNameMap = NNSPatriciaTreeReader.readTree(data);
		data.seek(paletteNameMapOffs);
		NNSPatriciaTreeReader.Entry[] paletteNameMap = NNSPatriciaTreeReader.readTree(data);

		for (NNSPatriciaTreeReader.Entry textureEntry : textureNameMap) {
			int value = textureEntry.getParam(0);
			if (!Utils.flagComp(value, 0x1000000)) {
				int offset = value & 0xFFFF;
				int matIndexCount = (value >> 16) & 0xFF;
				int unknown = value >>> 24;
				data.seek(offset);
				for (int i = 0; i < matIndexCount; i++) {
					materials.get(data.read()).textureName = textureEntry.getName();
				}
			}
		}
		for (NNSPatriciaTreeReader.Entry paletteEntry : paletteNameMap) {
			int value = paletteEntry.getParam(0);
			if (!Utils.flagComp(value, 0x1000000)) {
				int offset = value & 0xFFFF;
				int matIndexCount = (value >> 16) & 0xFF;
				data.seek(offset);
				for (int i = 0; i < matIndexCount; i++) {
					materials.get(data.read()).paletteName = paletteEntry.getName();
				}
			}
		}
		data.resetBase();
	}

	private void readMeshes(NTRDataIOStream data) throws IOException {
		data.setBaseHere();
		meshes = NNSPatriciaTreeReader.processOffsetTree(data, (io, entry) -> {
			return new NSBMDMesh(data, entry.getName());
		});
		data.resetBase();
	}

	private void readSBC(NTRDataIOStream data, int max) throws IOException {
		commands = new ArrayList();
		while (data.getPosition() < max) {
			int cmd = data.read();
			int opcode = BitMath.getIntegerBits(cmd, 0, 5);
			int flags = BitMath.getIntegerBits(cmd, 5, 3);

			switch (SBCCommand.SBCOpCode.valueOf(opcode)) {
				case NOP:
					commands.add(new NOP());
					break;
				case RET:
					return;
				case NODE:
					commands.add(new NODE(data));
					break;
				case MTX:
					commands.add(new MTX(data));
					break;
				case MAT:
					commands.add(new MAT(data, flags));
					break;
				case SHP:
					commands.add(new SHP(data));
					break;
				case NODEDESC:
					commands.add(new NODEDESC(data, flags));
					break;
				case BB:
					commands.add(new BB(data, flags));
					break;
				case BBY:
					commands.add(new BBY(data, flags));
					break;
				case NODEMIX:
					commands.add(new NODEMIX(data));
					break;
				case CALLDL:
					commands.add(new CALLDL(data));
					break;
				case POSSCALE:
					commands.add(new POSSCALE(flags));
					break;
				case ENVMAP:
					commands.add(new ENVMAP(data));
					break;
				case PRJMAP:
					commands.add(new PRJMAP(data));
					break;
				default:
					System.err.println("Unknown render opcode " + opcode);
					break;
			}
		}
	}

	public Model toGeneric(NSBMDImportSettings settings) {
		Model mdl = new Model();
		mdl.name = name;

		NSBMDModelConverter conv = new NSBMDModelConverter(this, settings);
		conv.matrixMode(MtxMode.GEMatrixMode.MODELVIEW_NORMAL);

		for (SBCCommand sbc : commands) {
			System.out.println(sbc);
			sbc.toGeneric(conv);
		}

		for (Material m : conv.materials) {
			mdl.addMaterial(m);
		}
		
		for (MeshVisibilityGroup visGroup : conv.visGroups) {
			mdl.addVisGroup(visGroup);
		}

		mdl.skeleton.addJoints(conv.joints);

		Map<String, Mesh> mergedMeshes = new HashMap<>();

		for (Mesh mesh : conv.meshes) {
			if (mesh.vertices.isEmpty()) {
				continue;
			}

			if (settings.mergeMeshesByMaterials) {
				String key = mesh.materialName + "_lay" + mesh.renderLayer + "_" + mesh.primitiveType;
				if (mesh.visGroupName != null) {
					key += "_" + mesh.visGroupName;
				}
				if (!mergedMeshes.containsKey(key)) {
					mergedMeshes.put(key, mesh);
					mesh.name = key;
					if (settings.makeSmoothSkin) {
						MeshProcessor.transformRigidSkinningToSmooth(mesh, mdl.skeleton, true);
					}
				} else {
					Mesh merge = mergedMeshes.get(key);
					merge.vertices.addAll(mesh.vertices);

					merge.hasColor |= mesh.hasColor;
					merge.hasNormal |= mesh.hasNormal;
					merge.hasUV[0] |= mesh.hasUV[0];
					merge.hasBoneIndices |= mesh.hasBoneIndices;
					merge.hasBoneWeights |= mesh.hasBoneWeights;

					if (merge.skinningType == Mesh.SkinningType.SMOOTH) {
						if (mesh.skinningType != Mesh.SkinningType.SMOOTH) {
							MeshProcessor.transformRigidSkinningToSmooth(mesh, mdl.skeleton, true);
						}
					} else {
						if (mesh.skinningType == Mesh.SkinningType.RIGID) {
							if (!merge.vertices.isEmpty()) {
								Vertex refVtx = mesh.vertices.get(0);
								Vertex refVtxMerge = merge.vertices.get(0);
								if (refVtx.boneIndices.size() == 1 && refVtxMerge.boneIndices.size() == 1) {
									if (!Objects.equals(refVtx.boneIndices.get(0), refVtxMerge.boneIndices.get(0))) {
										MeshProcessor.transformRigidSkinningToSmooth(merge, mdl.skeleton, true);
									}
								}
							}
						}
					}
				}
			} else {
				if (settings.makeSmoothSkin) {
					MeshProcessor.transformRigidSkinningToSmooth(mesh, mdl.skeleton, true);
				}
				mdl.addMesh(mesh);
			}
		}

		if (settings.mergeMeshesByMaterials) {
			//Use the list to preserve mesh order since we can not fully guess layering
			for (Mesh mesh : conv.meshes) {
				if (mergedMeshes.containsValue(mesh)) {
					mdl.addMesh(mesh);
				}
			}
		}

		return mdl;
	}

	public List<NSBMDMaterial> getMaterials() {
		return materials;
	}

	public NSBMDModelHeader getModelHeader() {
		return modelHeader;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}

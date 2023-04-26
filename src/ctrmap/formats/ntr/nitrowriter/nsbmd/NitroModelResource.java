package ctrmap.formats.ntr.nitrowriter.nsbmd;

import ctrmap.formats.ntr.nitrowriter.common.NNSWriterLogger;
import ctrmap.formats.ntr.common.FX;
import ctrmap.formats.ntr.common.gfx.Nitroshader;
import ctrmap.formats.ntr.nitrowriter.common.resources.NNSG3DResource;
import ctrmap.formats.ntr.nitrowriter.common.resources.NNSG3DResourceTree;
import ctrmap.formats.ntr.nitrowriter.common.resources.NNSPatriciaTreeWriter;
import ctrmap.formats.ntr.nitrowriter.nsbmd.dl.NitroMeshResource;
import ctrmap.formats.ntr.nitrowriter.nsbmd.dl.DLSubMeshFactory;
import ctrmap.formats.ntr.nitrowriter.nsbmd.mat.MaterialBlock;
import ctrmap.formats.ntr.nitrowriter.nsbmd.mat.NitroMaterialResource;
import ctrmap.formats.ntr.nitrowriter.nsbmd.sbc.SBC;
import ctrmap.formats.ntr.nitrowriter.nsbtx.TEX0;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.TextureMapper;
import ctrmap.renderer.util.QuadstripConverter;
import ctrmap.renderer.util.TristripConverter;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.PointerTable;
import xstandard.io.structs.TemporaryOffset;
import xstandard.io.structs.TemporaryValue;
import xstandard.io.util.StringIO;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Vec3f;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joml.Matrix3f;
import org.joml.Matrix4x3f;

/**
 *
 */
public class NitroModelResource extends NNSG3DResource {

	private static final int JOINTED_SUBMESH_MAX = 254;
	//The maximum joint index is 255, but so is the joint count... so the 255 index is impossible

	private final NNSWriterLogger log;

	private float posScale;
	private float invPosScale;

	private Vec3f bboxMin;
	private Vec3f bboxDim;

	private List<NitroJointResource> joints = new ArrayList<>();
	public final MaterialBlock materials = new MaterialBlock();
	private List<NitroMeshResource> meshes = new ArrayList<>();
	private SBC sbc;

	private List<FX.Mat4x3FX32> inverseBindMatrices = new ArrayList<>();
	private List<FX.Mat3x3FX32> inverseNormalMatrices = new ArrayList<>();

	public NitroModelResource(Model model, List<Texture> textures, NSBMDExportSettings settings, NNSWriterLogger log) {
		this.log = log;
		name = model.name;
		Skeleton skeleton = model.skeleton;

		bboxMin = model.boundingBox.min;
		bboxDim = model.boundingBox.getDimensions();
		Vec3f modelCenter = new Vec3f(new Vec3f(bboxDim).div(2).add(bboxMin));
		String rootJointName = "root_" + model.name;

		boolean meshConvCreateJoints = false;

		if (skeleton.getJoints().isEmpty()) {
			//The format does not provide us with a way to store matrices aside from node transforms
			//We could just write all vertices for the identity matrix, but since we are sure this model will not have animations,
			//we can use the node matrices for whatever we want.
			//Thus, we have the freedom of creating a joint for each mesh, possibly optimizing vertex cycles

			//TODO: Option to do this using MTX_LOAD instead (faster)
			//      - would need updating NitroReader to support MTX_LOAD tho
			meshConvCreateJoints = true;

			skeleton = new Skeleton();

			Joint root = new Joint();

			root.position = modelCenter;
			root.name = rootJointName;

			skeleton.addJoint(root);
		}

		List<Mesh> meshInput = new ArrayList<>();

		if (settings.makePolystrips) {
			for (Mesh mesh : model.meshes) {
				switch (mesh.primitiveType) {
					case QUADS:
						meshInput.addAll(QuadstripConverter.makeQuadstrips(new Mesh(mesh), settings.minimumPolystripPrimitiveCount));
						break;
					case TRIS:
						meshInput.addAll(TristripConverter.makeTristrips(new Mesh(mesh), settings.minimumPolystripPrimitiveCount));
						break;
					default:
						meshInput.add(mesh);
						break;
				}
			}
		} else {
			meshInput.addAll(model.meshes);
		}

		List<DLSubMeshFactory> submeshFactories = new ArrayList<>();

		List<Map.Entry<String, List<Mesh>>> meshesByMaterial = new ArrayList<>(getMeshesByMaterialName(meshInput).entrySet());

		meshesByMaterial.sort((o1, o2) -> {
			String s1 = o1.getKey();
			String s2 = o2.getKey();
			if (s1 != null && s2 != null) {
				return s1.compareTo(s2);
			}
			return (s1 == null) ? 1 : -1;
		});

		int subMeshCount = 0;

		int submeshMax = meshConvCreateJoints ? JOINTED_SUBMESH_MAX : 255;

		Material dummyMat = null;

		for (Map.Entry<String, List<Mesh>> meshesByMat : meshesByMaterial) {
			if (subMeshCount >= submeshMax) {
				log.err("SubMesh count over " + submeshMax + "! Some meshes will be omitted.");
				break;
			}
			List<Mesh> inMeshList = meshesByMat.getValue();
			if (!inMeshList.isEmpty()) {
				DLSubMeshFactory smFactory;
				if (meshConvCreateJoints) {
					Joint j = new Joint();
					j.name = "j_" + inMeshList.get(0).name;
					j.parentName = rootJointName;

					Vec3f meshCenter = new Vec3f();

					int emptyMeshCount = 0;

					for (Mesh mesh : inMeshList) {
						if (mesh.vertices.isEmpty()) {
							emptyMeshCount++;
							continue;
						}
						meshCenter.add(new Vec3f(mesh.calcMaxVector()).add(mesh.calcMinVector()).div(2));
					}

					meshCenter.div(inMeshList.size() - emptyMeshCount);

					j.position = meshCenter;
					j.position.sub(modelCenter);

					smFactory = new DLSubMeshFactory(model, inMeshList, settings, skeleton.getJointCount());
					skeleton.addJoint(j);
				} else {
					smFactory = new DLSubMeshFactory(model, inMeshList, settings);
				}

				Texture tex = null;

				Material material = model.getMaterialByName(meshesByMat.getKey());

				if (material == null) {
					log.err("Could not resolve a material for name " + meshesByMat.getKey() + ", using a dummy...");
					if (dummyMat != null) {
						material = dummyMat;
					} else {
						material = new Material();
						material.name = "DummyMaterial";
						dummyMat = material;
					}
				}

				if (!material.textures.isEmpty()) {
					TextureMapper tm = material.textures.get(0);
					tex = (Texture) Scene.getNamedObject(tm.textureName, textures);
				}

				smFactory.prepareSubMeshesForConversion(skeleton, tex);

				submeshFactories.add(smFactory);

				int vertexAlpha;
				if (!Nitroshader.isNshReady(material)) {
					vertexAlpha = smFactory.getAvgVertexAlpha();
				} else {
					vertexAlpha = Nitroshader.getNshAlphaValue255(material);
				}
				NitroMaterialResource existing = materials.findMaterialByNameAndVAlpha(material.name, vertexAlpha);
				if (existing == null) {
					//	System.out.println("new mat " + material.name + " a " + vertexAlpha);
					NitroMaterialResource matRsc = new NitroMaterialResource(materials.getMaterialCount(), material, textures, vertexAlpha);
					materials.addMaterial(matRsc);
					smFactory.attachMaterial(matRsc);
				} else {
					smFactory.attachMaterial(existing);
				}

				subMeshCount += smFactory.getSubMeshCount();
				smFactory.removeOverSubMeshes(subMeshCount - submeshMax);
				if (subMeshCount > submeshMax) {
					log.err("SubMeshes of mesh " + smFactory.meshName + " over limit (" + (subMeshCount - submeshMax) + "). Removing...");
				}
			}
		}

		determineBaseGlobalScale(submeshFactories);

		commitInvGlobalScale(submeshFactories);

		for (DLSubMeshFactory sf : submeshFactories) {
			meshes.addAll(sf.getMeshResources());
		}

		for (Joint j : skeleton) {
			//NOTE: The format actually only supports 64 joints for smooth skinning because of the
			//renderer memory structure. On each IBP matrix load, a bit is set in the structure to
			//prevent needlessly reloading the matrix. These bits are distributed among 2 32-bit fields,
			//allowing for a maximum of 64 matrices.
			// (0x20671B0 in W2U ARM9)
			joints.add(new NitroJointResource(j));

			Matrix4 invBind = skeleton.getAbsoluteJointBindPoseMatrix(j).invert();
			inverseBindMatrices.add(new FX.Mat4x3FX32(invBind.get4x3(new Matrix4x3f())));
			inverseNormalMatrices.add(new FX.Mat3x3FX32(invBind.normal(new Matrix3f())));
		}

		sbc = new SBC(materials.materials, meshes, skeleton, settings);
	}

	public void syncTEX0(TEX0 tex0) {
		for (NitroMaterialResource mat : materials.materials) {
			mat.syncTEX0(tex0);
		}
	}

	private static Map<String, List<Mesh>> getMeshesByMaterialName(List<Mesh> meshes) {
		Map<String, List<Mesh>> map = new HashMap<>();
		for (Mesh mesh : meshes) {
			List<Mesh> list = map.get(mesh.materialName);
			if (list == null) {
				list = new ArrayList<>();
				map.put(mesh.materialName, list);
			}
			list.add(mesh);
		}
		return map;
	}

	public void determineBaseGlobalScale(List<DLSubMeshFactory> smFactories) {
		Vec3f bboxMinAvg = getAllSubMeshesBBoxMinAvg(smFactories);
		Vec3f bboxMaxAvg = getAllSubMeshesBBoxMaxAvg(smFactories);

		float bboxMin = bboxMinAvg.getComponentAverage();
		float bboxMax = bboxMaxAvg.getComponentAverage();

		float center = (bboxMin + bboxMax) / 2f;

		float bboxMinOut = bboxMin;
		float bboxMaxOut = bboxMax;

		float minFromCenter = bboxMin - center;
		float maxFromCenter = bboxMax - center;

		for (DLSubMeshFactory smf : smFactories) {
			float minHighest = smf.getAllSubMeshesBBoxMin().getHighestAbsComponentNonAbs();
			float maxHighest = smf.getAllSubMeshesBBoxMax().getHighestAbsComponentNonAbs();

			if (minHighest < bboxMinOut) {
				if (Math.abs((minHighest - center) / minFromCenter) < 1.5f) {
					bboxMinOut = minHighest;
				}
			}

			if (maxHighest > bboxMaxOut) {
				if (Math.abs((maxHighest - center) / maxFromCenter) < 1.5f) {
					bboxMaxOut = maxHighest;
				}
			}
		}

		float outMax = Math.max(bboxMinOut, bboxMaxOut) + 4f; //tolerance of four to avoid yucky imprecisions
		if (outMax > FX.FX16_MAX) {
			posScale = outMax / FX.FX16_MAX;
			invPosScale = 1f / posScale;
		} else {
			posScale = 1f;
			invPosScale = 1f;
		}
	}

	public void commitInvGlobalScale(List<DLSubMeshFactory> smFactories) {
		for (DLSubMeshFactory smf : smFactories) {
			smf.applyInvGlobalScale(invPosScale);
		}
	}

	public static Vec3f getAllSubMeshesBBoxMinAvg(List<DLSubMeshFactory> smFactories) {
		Vec3f bboxMin = new Vec3f();
		for (DLSubMeshFactory smf : smFactories) {
			bboxMin.add(smf.getAllSubMeshesBBoxMin());
		}
		bboxMin.div(smFactories.size());
		return bboxMin;
	}

	public static Vec3f getAllSubMeshesBBoxMaxAvg(List<DLSubMeshFactory> smFactories) {
		Vec3f bboxMax = new Vec3f();
		for (DLSubMeshFactory smf : smFactories) {
			bboxMax.add(smf.getAllSubMeshesBBoxMax());
		}
		bboxMax.div(smFactories.size());
		return bboxMax;
	}

	@Override
	public byte[] getBytes() throws IOException {
		NNSG3DResourceTree.renameDuplicates(joints);
		NNSG3DResourceTree.renameDuplicates(materials.materials);
		NNSG3DResourceTree.renameDuplicates(meshes);

		if (joints.size() > 255) {
			log.err("WARNING: TOO MANY JOINTS (" + joints.size() + ") on model " + name + ". Trimming.");

			joints = joints.subList(0, 255);
		}

		if (materials.materials.size() > 255) {
			log.err("WARNING: TOO MANY MATERIALS (" + materials.materials.size() + ") on model " + name + ". Trimming.");

			materials.materials = materials.materials.subList(0, 255);
		}

		if (meshes.size() > 255) {
			log.err("WARNING: TOO MANY SUBMESHES (" + meshes.size() + ") on model " + name + ". Trimming.");

			meshes = meshes.subList(0, 255);
		}

		DataIOStream out = new DataIOStream();

		TemporaryValue size = new TemporaryValue(out);
		TemporaryOffset sbcOffset = new TemporaryOffset(out);
		TemporaryOffset matOffset = new TemporaryOffset(out);
		TemporaryOffset meshesOffset = new TemporaryOffset(out);
		TemporaryOffset invBindMtxOffset = new TemporaryOffset(out);

		//Model info
		out.write(0);
		out.write(1); //scaling mode
		out.write(0); //texture matrix mode
		out.write(joints.size());
		out.write(materials.getMaterialCount());
		out.write(meshes.size());
		out.write(sbc.mtxStackTop + 1);
		out.write(0);
		out.writeInt(FX.fx32(posScale));
		out.writeInt(FX.fx32(invPosScale));
		int vertexCount = 0;
		int faceCount = 0;
		int triCount = 0;
		int quadCount = 0;
		for (NitroMeshResource mesh : meshes) {
			vertexCount += mesh.vertexCount;
			faceCount += mesh.faceCount;
			triCount += mesh.triCount;
			quadCount += mesh.quadCount;
		}
		out.writeShort(vertexCount);
		out.writeShort(faceCount);
		out.writeShort(triCount);
		out.writeShort(quadCount);

		float bboxHighestNo = Math.max(bboxMin.getHighestAbsComponent(), bboxDim.getHighestAbsComponent()) + 4f;
		float bboxScale = 1f;
		float bboxInvScale;
		if (bboxHighestNo > FX.FX16_MAX) {
			bboxScale = bboxHighestNo / FX.FX16_MAX;
		}
		bboxInvScale = 1f / bboxScale;

		new FX.VecFX16(bboxMin, bboxInvScale).write(out);
		new FX.VecFX16(bboxDim, bboxInvScale).write(out);

		out.writeInt(FX.fx32(bboxScale));
		out.writeInt(FX.fx32(bboxInvScale));

		//Skeleton
		NNSG3DResourceTree jointTree = new NNSG3DResourceTree();
		for (NitroJointResource jr : joints) {
			jointTree.addResource(jr);
		}
		jointTree.write(out, -out.getPosition());

		//SBC
		out.pad(4);
		sbcOffset.setHere();
		sbc.write(out);

		//Materials
		out.pad(4);
		matOffset.setHere();
		out.write(materials.getBytes());

		//Sub-meshes
		out.pad(4);
		meshesOffset.setHere();
		List<TemporaryOffset> displayListOffsets = new ArrayList<>();
		List<TemporaryValue> displayListSizes = new ArrayList<>();

		int meshTreeOffBase = -out.getPosition();
		NNSPatriciaTreeWriter.writeNNSPATRICIATree(out, meshes, 4);

		List<TemporaryOffset> meshOffsets = PointerTable.allocatePointerTable(meshes.size(), out, meshTreeOffBase, false);
		for (NitroMeshResource mesh : meshes) {
			StringIO.writePaddedString(out, mesh.name, 16);
		}

		for (int i = 0; i < meshes.size(); i++) {
			out.pad(4);
			meshOffsets.get(i).setHere();
			NitroMeshResource mesh = meshes.get(i);
			int dlOffBase = -out.getPosition();
			out.writeShort(0);
			out.writeShort(NitroMeshResource.HEADER_BYTES);
			out.writeInt(mesh.getDLFlags());
			displayListOffsets.add(new TemporaryOffset(out, dlOffBase));
			displayListSizes.add(new TemporaryValue(out));
		}

		for (int i = 0; i < meshes.size(); i++) {
			out.pad(4);
			displayListOffsets.get(i).setHere();
			int pos = out.getPosition();
			meshes.get(i).displayList.write(out);
			displayListSizes.get(i).set(out.getPosition() - pos);
		}

		//IBP matrices
		out.pad(4);
		invBindMtxOffset.setHere();
		for (int i = 0; i < inverseBindMatrices.size(); i++) {
			inverseBindMatrices.get(i).write(out);
			inverseNormalMatrices.get(i).write(out);
		}

		size.set(out.getLength());

		out.close();
		return out.toByteArray();
	}

}

package ctrmap.missioncontrol_ntr.field;

import ctrmap.formats.ntr.common.FX;
import ctrmap.formats.ntr.common.gfx.GXColor;
import ctrmap.renderer.scene.Light;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialColorType;
import ctrmap.renderer.scene.texturing.TexEnvStage;
import ctrmap.renderer.scenegraph.G3DResourceInstance;
import ctrmap.renderer.util.generators.PlaneGenerator;
import xstandard.math.vec.RGBA;
import java.util.ArrayList;
import java.util.List;
import ctrmap.formats.pokemon.gen5.area.AreaLightFile;
import ctrmap.missioncontrol_ntr.VRTC;
import ctrmap.missioncontrol_ntr.field.structs.VArea;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import org.joml.AxisAngle4f;
import xstandard.math.MathEx;
import xstandard.math.vec.Vec3f;

public class VFieldLightSystem {

	private final List<Light> lights = new ArrayList<>();

	private NTRGameFS fs;

	private AreaLightFile lightFile;
	private AreaLightFile.AreaLightEntry curEntry;
	private VRTC.Season season = VRTC.Season.getRTC();

	private final Model skyBgModel = new Model();
	private final RGBA skyBgColorRef = new RGBA();

	private Interpolator interpolator;

	public VFieldLightSystem(NTRGameFS fs) {
		this.fs = fs;

		for (int i = 0; i < 4; i++) {
			lights.add(null);
		}

		Mesh mesh = PlaneGenerator.generateQuadPlaneMesh(1f, 1f, 0, true, true);
		mesh.name = "ClearColorPlaneMesh";
		Material material = new Material();
		material.addShaderExtension("SkyClearColorPlaneShader.vsh_ext");
		material.name = "FieldLightSystem_ClearColorPlane";
		mesh.materialName = material.name;
		material.tevStages.stages[0] = new TexEnvStage(TexEnvStage.TexEnvTemplate.CCOL);
		material.tevStages.stages[0].constantColor = MaterialColorType.CONSTANT0;
		material.constantColors[0] = skyBgColorRef;
		material.depthColorMask.enabled = false;
		skyBgModel.addMesh(mesh);
		skyBgModel.addMaterial(material);
		skyBgModel.name = "ClearColorPlane";
	}

	public void loadArea(VArea area, VRTC.Season season) {
		this.season = season;
		curEntry = null;
		if (area.header != null) {
			lightFile = new AreaLightFile(fs.NARCGet(NARCRef.FIELD_ENV_LIGHTS, area.header.lightIndex));
			interpolator = new Interpolator();
		} else {
			lightFile = null;
			interpolator = null;
		}
	}

	private void buildLights() {
		if (lightFile != null && interpolator != null) {
			AreaLightFile.AreaLightEntry newEntry = lightFile.getLightEntryBySeconds(season, VRTC.getSecondOfDay());
			if (newEntry != curEntry) {
				interpolator.interpolateTo(newEntry, 2000);
				curEntry = newEntry;
			}
			if (interpolator.isRunning()) {
				AreaLightFile.AreaLightEntry e = interpolator.update();

				for (int i = 0; i < 4; i++) {
					if (e.lightsEnabled[i]) {
						Light l = lights.get(i);
						if (l == null) {
							l = new Light("VFieldLight_" + i);
						}

						l.setIndex = i;
						l.directional = true;
						l.diffuseColor.set(e.colors[i].toRGBA());
						l.ambientColor.set(l.diffuseColor);
						l.direction.set(e.directions[i]);
						corruptFixedPointLightVector(l.direction);

						lights.set(i, l);
					} else {
						lights.set(i, null);
					}
				}

				skyBgColorRef.set(e.clearColor.toRGBA());
			}
		} else {
			for (int i = 0; i < 4; i++) {
				lights.set(i, null);
			}
		}
	}

	private static void corruptFixedPointLightVector(Vec3f vec) {
		vec.x = corruptFixedPointLightValue(vec.x);
		vec.y = corruptFixedPointLightValue(vec.y);
		vec.z = corruptFixedPointLightValue(vec.z);
	}

	private static float corruptFixedPointLightValue(float value) {
		int fixed = FX.fx(value, 1, 9);
		float unfixed = FX.unfx(fixed, 1, 9);
		return unfixed;
	}

	public void updateScene(G3DResourceInstance scene) {
		buildLights();

		for (int i = 0; i < scene.lights.size(); i++) {
			if (!lights.contains(scene.lights.get(i))) {
				scene.lights.remove(i);
				i--;
			}
		}
		scene.instantiateLights(lights);
		if (lightFile != null) {
			if (!scene.resource.models.contains(skyBgModel)) {
				scene.resource.addModel(skyBgModel);
			}
		} else {
			scene.resource.models.remove(skyBgModel);
		}
	}

	private static class Interpolator {

		private final AxisAngle4f[] interpAA = new AxisAngle4f[4];
		private final float[] interpAngles = new float[4];

		private AreaLightFile.AreaLightEntry src;
		private AreaLightFile.AreaLightEntry dst;
		private AreaLightFile.AreaLightEntry cur;
		private long interpStart = 0;
		private long interpPeriod = 0;

		public Interpolator() {
			for (int i = 0; i < 4; i++) {
				interpAA[i] = new AxisAngle4f();
			}
		}

		public void interpolateTo(AreaLightFile.AreaLightEntry dst, long msInterval) {
			this.dst = dst;
			if (cur != null) {
				interpPeriod = msInterval;
				interpStart = System.currentTimeMillis();
				this.src = cur;
				this.cur = new AreaLightFile.AreaLightEntry(cur);
				for (int i = 0; i < 4; i++) {
					Vec3f dir1 = src.directions[i];
					Vec3f dir2 = dst.directions[i];
					Vec3f axis = new Vec3f();
					dir1.cross(dir2, axis);
					axis.normalize();
					interpAA[i].set(0f, axis);
					interpAngles[i] = dir1.angle(dir2);
				}
			} else {
				interpPeriod = 0;
			}
		}

		public AreaLightFile.AreaLightEntry update() {
			if (isRunning()) {
				float weight = interpPeriod == 0f ? 1f : MathEx.clamp(0f, 1f, (System.currentTimeMillis() - interpStart) / (float) interpPeriod);
				if (weight >= 1f) {
					cur = dst;
					dst = null;
					src = null;
				} else if (src != null) {
					System.arraycopy(src.lightsEnabled, 0, cur.lightsEnabled, 0, 4);
					for (int i = 0; i < 4; i++) {
						cur.directions[i] = new Vec3f(src.directions[i]);
						cur.directions[i].normalize();
						interpAA[i].angle = interpAngles[i] * weight;
						if (interpAA[i].angle >= 0.001f) {
							interpAA[i].transform(src.directions[i]);
						}

						cur.colors[i] = new GXColor(src.colors[i], dst.colors[i], weight);
					}
					cur.matDiffuse = new GXColor(src.matDiffuse, dst.matDiffuse, weight);
					cur.matAmbient = new GXColor(src.matAmbient, dst.matAmbient, weight);
					cur.matSpecular = new GXColor(src.matSpecular, dst.matSpecular, weight);
					cur.matEmission = new GXColor(src.matEmission, dst.matEmission, weight);
					cur.fogColor = new GXColor(src.fogColor, dst.fogColor, weight);
					cur.clearColor = new GXColor(src.clearColor, dst.clearColor, weight);
				}
			}
			return cur;
		}

		public boolean isRunning() {
			return dst != null;
		}
	}
}

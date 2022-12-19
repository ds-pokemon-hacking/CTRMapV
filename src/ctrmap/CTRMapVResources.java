package ctrmap;



import xstandard.res.ResourceAccess;
import java.io.File;
import xstandard.res.ResourceAccessor;

public class CTRMapVResources {
	
	public static final ResourceAccessor ACCESSOR = new ResourceAccessor("ctrmap/resources");
	
	public static void load(){
		ResourceAccess.loadResourceTable(CTRMapVResources.class.getClassLoader(), "ctrmap/resources/res_ctrmap_V.tbl");
	}

	public static void main(String[] args) {
		ResourceAccess.buildResourceTable(new File("src/ctrmap/resources"), "ctrmap/resources", "res_ctrmap_V.tbl");
	}
}

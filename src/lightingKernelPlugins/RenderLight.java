package lightingKernelPlugins;

import static org.jocl.CL.clEnqueueNDRangeKernel;
import static org.jocl.CL.clSetKernelArg;

import org.jocl.Pointer;
import org.jocl.Sizeof;

import tbh.gfxInterface.KernelPlugin;

public class RenderLight extends KernelPlugin{

	@Override
	public void run(Object[] arg0) {
		int canvas, texture, normals, x, y, radius, color, width, height;
		float lightDir, angleWidth, angularSharpness, lightIntensity, lightVolume, radialInfluence, angularInfluence, normalInfluence;
		//IntArrayImage dest;
		
		try {
			canvas = (int) arg0[0];
			texture = (int) arg0[1];
			normals = (int) arg0[2];
			x = (int) arg0[3];
			y = (int) arg0[4];
			radius = (int) arg0[5];
			color = (int) arg0[6];
			lightDir = (float) arg0[7];
			angleWidth = (float) arg0[8];
			angularSharpness = (float) arg0[9]; 
			lightIntensity = (float) arg0[10];
			lightVolume = (float) arg0[11];
			radialInfluence = (float) arg0[12];
			angularInfluence = (float) arg0[13];
			normalInfluence = (float) arg0[14];
			Class intArrayImgExists = Class.forName("memoryPlugins.IntArrayImage");
			//dest = (IntArrayImage) gfx.getMemoryPlugin(canvas);
			width = (int) gfx.getMemoryPlugin(canvas).getClass().getMethod("getWidth").invoke(gfx.getMemoryPlugin(canvas));
			height = (int) gfx.getMemoryPlugin(canvas).getClass().getMethod("getHeight").invoke(gfx.getMemoryPlugin(canvas));
		} catch (ClassNotFoundException e) {
			gfx.GfxLog(2, "Cannot run plugin RenderLight, missing dependency \"memoryPlugins.IntArrayImage\"");
			e.printStackTrace();
			return;
		} catch (NoClassDefFoundError e) {
			gfx.GfxLog(2, "Cannot run plugin RenderLight, missing class definition for \"memoryPlugins.IntArrayImage\"");
			e.printStackTrace();
			return;
		} catch (Exception e){
			gfx.GfxLog(2, "Illegal Arguments for RenderLight plugin, args are:\n"
					+ "int canvas\n"
					+ "int texture\n"
					+ "int normals\n"
					+ "int x\n"
					+ "int y\n"
					+ "int radius\n"
					+ "int color\n"
					+ "float lightDir\n"
					+ "float angleWidth\n"
					+ "float angularSharpness\n"
					+ "float lightIntensity\n"
					+ "float lightVolume\n"
					+ "float radialInfluence\n"
					+ "float angularInfluence\n"
					+ "float normalInfluence");
			e.printStackTrace();
			return;
		}
		
		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(gfx.getMemoryObject(canvas)));
		clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(gfx.getMemoryObject(texture)));
		clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(gfx.getMemoryObject(normals)));
		clSetKernelArg(kernel, 3, Sizeof.cl_int2, Pointer.to(new int[] {width, height}));
		clSetKernelArg(kernel, 4, Sizeof.cl_int2, Pointer.to(new int[] {x, y}));
		clSetKernelArg(kernel, 5, Sizeof.cl_uint, Pointer.to(new int[] {radius}));
		clSetKernelArg(kernel, 6, Sizeof.cl_uint, Pointer.to(new int[] {color}));
		clSetKernelArg(kernel, 7, Sizeof.cl_float, Pointer.to(new float[] {lightDir%360}));
		clSetKernelArg(kernel, 8, Sizeof.cl_float, Pointer.to(new float[] {angleWidth}));
		clSetKernelArg(kernel, 9, Sizeof.cl_float, Pointer.to(new float[] {angularSharpness}));
		clSetKernelArg(kernel, 10, Sizeof.cl_float, Pointer.to(new float[] {lightIntensity}));
		clSetKernelArg(kernel, 11, Sizeof.cl_float, Pointer.to(new float[] {lightVolume}));
		clSetKernelArg(kernel, 12, Sizeof.cl_float4, Pointer.to(new float[] {radialInfluence, angularInfluence, normalInfluence, 0f}));
			
		long local_work_size[] = new long[]{1, 1};
		long global_work_size[] = new long[]{ (long) (2*radius), (long) (2 * radius)};

		int err = clEnqueueNDRangeKernel(gfx.getCommandQueue(), kernel, 2, null, global_work_size, local_work_size, 0, null, null);
		if (err != org.jocl.CL.CL_SUCCESS) gfx.GfxLog(2, "Failed to render light: " + org.jocl.CL.stringFor_errorCode(err));
		gfx.runPlugin("DrawLine", new Object[] {x, y, x + (int)(Math.sin(lightDir * 0.01745f)*30), y + (int)(Math.cos(lightDir * 0.01745f)*30), 0xffff0000, canvas});
		gfx.updateResource(canvas);
	}

}

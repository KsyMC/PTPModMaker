package com.ksymc.ptpmodmaker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class PTPatch {
    public final static byte[] magic = {(byte) 0xff, 0x50, 0x54, 0x50};
    public File patchfile;
	byte[] patch_array;
	byte[] patchdata;
	byte[] indices;
	byte[] auther;

    public PTPatch(File file) {
        patchfile = file;
    }
 
    public boolean load() throws Exception {
    	patch_array = new byte[(int) patchfile.length()];
    	InputStream is = new FileInputStream(patchfile);
        is.read(patch_array);
        is.close();

		if(Arrays.equals(Arrays.copyOf(patch_array, 4), magic)) {
			int indicesEnd = (patch_array[5] * 4) + 6;
            indices = Arrays.copyOfRange(patch_array, 6, indicesEnd);
            int startPathData = getDataIndex(0);
			auther = (indicesEnd != startPathData ? Arrays.copyOfRange(patch_array, indicesEnd, startPathData) : "".getBytes());
            patchdata = Arrays.copyOfRange(patch_array, startPathData, patch_array.length);
            return true;
        }
        return false;
    }

	public int getMCPEVersion() {
		return patch_array[4];
	}

	public int getNumPatches() {
		return patch_array[5];
	}

	public byte[] getAuther() {
		return auther;
	}

	public int getDataIndex(int index) {
		int base = (4 * index);
		return Utils.byteArrayToInt(new byte[]{
			indices[base],
			indices[base + 1],
			indices[base + 2],
			indices[base + 3]
		});
	}

	public byte[][] getData() {
		int size = getNumPatches();
		byte[][] data = new byte[size][];
		for (int i = 0; i < size; i++) {
			int index = getDataIndex(i);
			int nextindex = (i + 1 == size ? patch_array.length : getDataIndex(i + 1));
			data[i] = Arrays.copyOfRange(patch_array, index, nextindex);
		}
		return data;
	}

    public void write(int version, int numpatches, String auther, byte[][] datas) throws Exception {
		byte[] indices = new byte[datas.length * 4];
		int in = 6 + auther.getBytes("UTF-8").length + (numpatches * 4);
		for (int i = 0; i < numpatches; i++) {
			byte[] index = Utils.intToByteArray(in);
			int base = i * 4;
			indices[base] = index[0];
			indices[base + 1] = index[1];
			indices[base + 2] = index[2];
			indices[base + 3] = index[3];

			in += datas[i].length;
		}

		OutputStream os = new FileOutputStream(patchfile);
        try {
            os.write(magic);
            os.write(version);
            os.write(numpatches);
            os.write(indices);
            os.write(auther.getBytes());
			for (byte[] data : datas) {
				os.write(data);
			}
        } finally {
        	os.close();
        }
    }
}

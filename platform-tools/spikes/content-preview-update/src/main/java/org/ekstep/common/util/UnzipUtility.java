package org.ekstep.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

public class UnzipUtility {

	public void unzip(InputStream in, String destDirectory) throws IOException {
		File destDir = new File(destDirectory);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		ZipInputStream zipIn = new ZipInputStream(in);
		unzip(zipIn, destDirectory);
	}

	public void unzip(String zipFilePath, String destDirectory) throws IOException {
		File destDir = new File(destDirectory);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
		unzip(zipIn, destDirectory);
	}

}
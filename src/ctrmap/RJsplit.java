/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ctrmap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.fs.accessors.DiskFile;

/**
 *
 */
public class RJsplit {

	public static String stripAccents(String s) {
		s = Normalizer.normalize(s, Normalizer.Form.NFD);
		s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
		return s;
	}

	private static void flush(List<String> lines, String j, String s) {
		if (j != null && s != null) {
			System.out.println("flushing " + j + "/" + s);
			if (!lines.isEmpty()) {
				j = j.replace(".", "");
				s = s.replace(".", "");
				lines.add(0, j + " " + s);
				try {
					Files.write(Paths.get("D:\\Work\\AloyzEE\\hamlet\\", stripAccents(j + " " + s + ".txt")), lines, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
				} catch (IOException ex) {
					Logger.getLogger(RJsplit.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		lines.clear();
	}

	public static void main(String[] args) {
		Scanner s = new Scanner(new DiskFile("D:\\Work\\AloyzEE\\hamlet.txt").getNativeInputStream());
		String jednani = null;
		String scena = null;
		List<String> curLines = new ArrayList<>();
		while (s.hasNextLine()) {
			String line = s.nextLine().trim();
			if (!line.isEmpty()) {
				if (line.contains("JEDNÁNÍ") || line.contains("JEDNÁNI")) {
					flush(curLines, jednani, scena);
					jednani = line;
				} else if (line.contains("Scéna") || line.contains("Scena")) {
					flush(curLines, jednani, scena);
					scena = line;
				} else {
					curLines.add(line);
				}
			}
		}
		flush(curLines, jednani, scena);
		s.close();
	}
}

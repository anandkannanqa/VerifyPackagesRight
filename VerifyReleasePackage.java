import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class VerifyReleasePackage {

	public static List<String> manifest;
	public static List<String> additional = new LinkedList<String>();
	public static List<String> matched = new LinkedList<String>();
	
	public static void main(String[] args) throws IOException {
		if(!new File("manifest.txt").exists())
			createManifest();
		else {
			manifest = getManifestEntries();
			compareManifest();
		}
	}

	public static void createManifest() throws IOException {
		ZipFile releasePackage = new ZipFile(findZipPath());
		Enumeration<? extends ZipEntry> entries = releasePackage.entries();
		PrintWriter writer = new PrintWriter("manifest.txt", "UTF-8");
		while(entries.hasMoreElements()) {
			String entry = strippedIfNotEmpty(entries.nextElement().getName());
			writer.println(entry);
			System.out.println(entry);
		}
		writer.close();
		releasePackage.close();
	}

	public static String findZipPath() {
		File root = new File(".");
		for(String file : root.list())
			if(file.endsWith(".zip"))
				return file;
		return null;
	}
	
	private static String strippedIfNotEmpty(String path) {
		String stripped = path.substring(path.indexOf("/") + 1);
		if(stripped.isEmpty())
			return path;
		return stripped;
	}

	public static List<String> getManifestEntries() throws IOException {
		List<String> entries = new LinkedList<String>();
		BufferedReader reader = new BufferedReader(new FileReader("manifest.txt"));
		String line;
		while((line = reader.readLine()) != null)
			entries.add(line);
		reader.close();
		return entries;
	}
	
	private static void compareManifest() throws IOException, FileNotFoundException {
		if(findZipPath() == null)
		{
			System.out.println("No release package .zip file found in folder to compare to");
		}
		else
		{
			ZipFile releasePackage = new ZipFile(findZipPath());
			Enumeration<? extends ZipEntry> entries = releasePackage.entries();
			
			while(entries.hasMoreElements())
				compare(strippedIfNotEmpty(entries.nextElement().getName()));
			
			printListInReadIfNotEmpty(manifest, "\nMissing:");		
			printIfNotEmpty(additional, "\nAdditional:");		
			printIfNotEmpty(matched, "\nMatched:");
			
			releasePackage.close();
		}
		
		if(!manifest.isEmpty() || !additional.isEmpty())
			System.exit(1);
	}

	private static void compare(String entry) {
		if(!entry.contains("WebGUI/bin/App_Web_")) //Skip these auto generated files
		{
			if(manifest.contains(entry))
			{
				manifest.remove(entry);
				matched.add(entry);
			}
			else if(matchAndRemove(entry))
				matched.add(entry);
			else
				additional.add(entry);
		}
	}

	private static boolean matchAndRemove(String entry) {
		for(String regex : manifest)
			if(entry.matches(regex)) {
				manifest.remove(regex);
				return true;
			}
		return false;
	}

	private static void printIfNotEmpty(List<String> list, String header) {
		if(!list.isEmpty()) {
			System.out.println(header);
			for(String entry : list)
				System.out.println(entry);
		}
	}
	
	private static void printListInReadIfNotEmpty(List<String> list, String header) {
		if(!list.isEmpty()) {
			System.err.println(header);
			for(String entry : list)
				System.err.println(entry);
		}
	}
	
}

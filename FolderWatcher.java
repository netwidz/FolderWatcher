package folderWatcher;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class FolderWatcher {

	private static final String path = "C:\\Users\\Fahiz\\tmp";

	public static void watchDirectoryPath(Path path) {
		// Sanity check - Check if path is a folder
		try {
			Boolean isFolder = (Boolean) Files.getAttribute(path,
					"basic:isDirectory", NOFOLLOW_LINKS);
			if (!isFolder) {
				throw new IllegalArgumentException("Path: " + path
						+ " is not a folder");
			}
		} catch (IOException ioe) {
			// Folder does not exists
			ioe.printStackTrace();
		}

		System.out.println("Watching path: " + path);

		// We obtain the file system of the Path
		FileSystem fs = path.getFileSystem();

		// We create the new WatchService using the new try() block
		try (WatchService service = fs.newWatchService()) {

			// We register the path to the service
			// We watch for creation events
			path.register(service, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
			// path.register(service, StandardWatchEventKinds.ENTRY_MODIFY);
			// path.register(service, StandardWatchEventKinds.ENTRY_DELETE);

			// Start the infinite polling loop
			WatchKey key = null;
			while (true) {
				key = service.take();

				// Dequeueing events
				Kind<?> kind = null;
				for (WatchEvent<?> watchEvent : key.pollEvents()) {
					// Get the type of the event
					kind = watchEvent.kind();
					if (OVERFLOW == kind) {
						continue; // loop
					} else if (ENTRY_CREATE == kind) {
						// A new Path was created
						
						
						Path newPath = ((WatchEvent<Path>) watchEvent)
								.context();
						
						Path fullPath = ((Path) key.watchable()).resolve(newPath);
						System.out.println("fullPath = "+fullPath);
						// Output
						try {

							
							
							File xmlFile =   fullPath.toFile();

							if (isXML(xmlFile)) {
								DocumentBuilderFactory dbFactory = DocumentBuilderFactory
										.newInstance();
								DocumentBuilder dBuilder = dbFactory
										.newDocumentBuilder();
								Document doc = dBuilder.parse(xmlFile);
							
								TransformerFactory tf = TransformerFactory.newInstance();
								Transformer transformer = tf.newTransformer();
								transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
								StringWriter writer = new StringWriter();
								transformer.transform(new DOMSource(doc), new StreamResult(writer));
								String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
								
								
								System.out.println(output);
							}

						
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					} else if (ENTRY_MODIFY == kind) {
						// modified
						Path newPath = ((WatchEvent<Path>) watchEvent)
								.context();
						// Output
						System.out.println("New path modified: " + newPath);
					} else if (ENTRY_DELETE == kind) {
						// modified
						Path newPath = ((WatchEvent<Path>) watchEvent)
								.context();
						// Output
						System.out.println("New path modified: " + newPath);
					}
				}

				if (!key.reset()) {
					break; // loop
				}
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}

	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		// Folder we are going to watch
		// Path folder =
		// Paths.get(System.getProperty("C:\\Users\\Isuru\\Downloads"));
		File dir = new File(path);
		watchDirectoryPath(dir.toPath());
	}

	private static Boolean isXML(File file) {
		Boolean flag = false;

		if (file.exists() && file.isFile()) {
			String fileName = file.getName();
			if (fileName.lastIndexOf(".") != -1
					&& fileName.lastIndexOf(".") != 0) {
				String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
				if (ext.equalsIgnoreCase("xml")) {
					return true;
				}

			}
		}
		return flag;

	}
}

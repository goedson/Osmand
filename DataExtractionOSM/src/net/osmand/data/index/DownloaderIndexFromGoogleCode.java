package net.osmand.data.index;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import net.osmand.LogUtil;

import org.apache.commons.logging.Log;


public class DownloaderIndexFromGoogleCode {

	private final static Log log = LogUtil.getLog(DownloaderIndexFromGoogleCode.class);
	/**
	 * @param args
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws URISyntaxException, IOException {
//		Map<String, String> indexFiles = DownloaderIndexFromGoogleCode.getIndexFiles(
//				new String[] { IndexConstants.ADDRESS_INDEX_EXT,	IndexConstants.POI_INDEX_EXT, IndexConstants.TRANSPORT_INDEX_EXT,
//						IndexConstants.ADDRESS_INDEX_EXT_ZIP,	IndexConstants.POI_INDEX_EXT_ZIP, IndexConstants.TRANSPORT_INDEX_EXT_ZIP,}, 
//				new String[] {	IndexConstants.ADDRESS_TABLE_VERSION + "", IndexConstants.POI_TABLE_VERSION + "",  //$NON-NLS-1$//$NON-NLS-2$
//								IndexConstants.TRANSPORT_TABLE_VERSION + "" , //$NON-NLS-1$
//								IndexConstants.ADDRESS_TABLE_VERSION + "", IndexConstants.POI_TABLE_VERSION + "",  //$NON-NLS-1$//$NON-NLS-2$
//								IndexConstants.TRANSPORT_TABLE_VERSION + "" }); //$NON-NLS-1$
//		System.out.println(indexFiles);

//		deleteFileFromGoogleDownloads("Bahrain_0.trans.odb", "vics001", "kfvth85");
		deleteFileFromGoogleDownloads("Bahrain_0.trans.odb", "vics001", "cH5mK8aA5kd3");
		
		
		
	}
	
	private static Map<String, String> getContent(String[] ext, String[] version) {
		Map<String, String> files = new TreeMap<String, String>();
		try {
			URL url = new URL("http://code.google.com/p/osmand/downloads/list?num=1500&start=0"); //$NON-NLS-1$

			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String s = null;
			String prevFile = null;
			while ((s = reader.readLine()) != null) {
				for(int i=0; i<ext.length; i++){
					prevFile = getIndexFiles(files, s, prevFile, ext[i], version[i]);
				}
			}
		} catch (MalformedURLException e) {
			log.error("Unexpected exception", e); //$NON-NLS-1$
		} catch (IOException e) {
			log.error("Input/Output exception", e); //$NON-NLS-1$
			
		}
		return files;
	}
	

	private static String getIndexFiles(Map<String, String> files, String content, String prevFile, String ext, String version){
		int i = 0;
		int prevI = -1;
		if((i = content.indexOf(ext, i)) != -1) {
			if(prevI > i){
				files.put(prevFile, null);
				prevI = i;
			}
			int j = i - 1;
			while (content.charAt(j) == '_' || Character.isLetterOrDigit(content.charAt(j)) || content.charAt(j) == '-') {
				j--;
			}
			if(content.substring(j + 1, i).endsWith("_"+version)){ //$NON-NLS-1$
				prevFile = content.substring(j + 1, i) + ext;
			}
			
		}
		if (prevFile != null && ((i = content.indexOf('{')) != -1)) {
			int j = content.indexOf('}');
			if (j != -1 && j - i < 40) {
				String description = content.substring(i, j + 1);
				files.put(prevFile, description);
			}
		}
		return prevFile;
	}
	
	public static Map<String, String> getIndexFiles(String[] ext, String[] version){
		return getContent(ext, version);
	}
	
	public static Map<String, String> getIndexFiles(String ext, String version){
		return getContent(new String[]{ext}, new String[]{version});
	}
	
	public static URL getInputStreamToLoadIndex(String indexName) throws IOException{
		URL url = new URL("http://osmand.googlecode.com/files/"+indexName); //$NON-NLS-1$
		return url;
	}
	
	
	// that method doesn't work !!!
	public static String deleteFileFromGoogleDownloads(String fileName, String userName, String password) throws IOException {
		// prepare data
		String urlText = "http://code.google.com/p/osmand/downloads/delete.do?name="+fileName;
		System.out.println(urlText);
		StringBuilder requestBody = new StringBuilder();
		requestBody.
				append("token=cc4e4cffba19a6107d2161f85c71631f").
//				append("token=").append(Base64.encode(token.getBytes("UTF-8"))).
				append("&pagegen=1283283182175778").
				append("&filename=").append(fileName).
				append("&delete=Delete+download");
		System.out.println(requestBody);
		
		// getting url
		URL url = new URL(urlText);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setConnectTimeout(15000);
		connection.setRequestMethod("POST");
//		String token = userName + ":" + password; //$NON-NLS-1$
//		connection.addRequestProperty("Authorization", "Basic " + Base64.encode(token.getBytes("UTF-8"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Content-Length", requestBody.length()+"");
		
		connection.setDoInput(true);
		connection.setDoOutput(true);
		
		connection.connect();
		
		
		OutputStream out = connection.getOutputStream();
		if (requestBody != null) {
			BufferedWriter bwr = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"), 1024); //$NON-NLS-1$
			bwr.write(requestBody.toString());
			bwr.flush();
			bwr.close();
		}
		
		log.info("Response : " + connection.getResponseMessage()); //$NON-NLS-1$
		// populate return fields.
		StringBuilder responseBody = new StringBuilder();
		InputStream i = connection.getInputStream();
		if (i != null) {
			BufferedReader in = new BufferedReader(new InputStreamReader(i, "UTF-8"), 256); //$NON-NLS-1$
			String s;
			boolean f = true;
			while ((s = in.readLine()) != null) {
				if(!f){
					responseBody.append("\n"); //$NON-NLS-1$
				} else {
					f = false;
				}
				responseBody.append(s);
			}
		}
		return responseBody.toString();
		
		
	}

}
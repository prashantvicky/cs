/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */

package com.db.comserv.main.utilities;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.db.comserv.main.utilities.servlet.ServletUtil;

@Component
public class HttpCaller implements IHttpCaller {

	@Autowired
	ServletContext servletContext;

	@Autowired
	DataSettingProperties properties;

	protected static final Logger sLog = LoggerFactory.getLogger("REQUEST");

	@Override
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "DM_DEFAULT_ENCODING")
	public HttpResult runRequest(String type, String methodType, URL url,
			List<Map<String, String>> headers, String requestBody,
			String sslByPassOption, int connTimeOut, int readTimeout,HttpServletRequest req)
			throws KeyManagementException, NoSuchAlgorithmException,
			KeyStoreException, UnsupportedEncodingException, IOException,
			UnknownHostException, URISyntaxException {

		StringBuffer response = new StringBuffer();
		HttpResult httpResult = new HttpResult();
		boolean gzip = false;
		final long startNano = System.nanoTime();
		try {
			URL encodedUrl = new URL(Utility.encodeUrl(url.toString()));
			HttpURLConnection con = (HttpURLConnection) encodedUrl.openConnection();
			TrustModifier.relaxHostChecking(con, sslByPassOption);

			// connection timeout 5s
			con.setConnectTimeout(connTimeOut);

			// read timeout 10s
			con.setReadTimeout(readTimeout*getQueryCost(req));

			methodType = methodType.toUpperCase();
			con.setRequestMethod(methodType);

			sLog.debug("Performing '{}' to '{}'", methodType,
					ServletUtil.filterUrl(url.toString()));

			// Get headers & set request property
			for (int i = 0; i < headers.size(); i++) {
				Map<String, String> header = headers.get(i);
				con.setRequestProperty(header.get("headerKey").toString(),
						header.get("headerValue").toString());
				sLog.debug("Setting Header '{}' with value '{}'",
						header.get("headerKey").toString(), ServletUtil
								.filterHeaderValue(header.get("headerKey")
										.toString(), header.get("headerValue")
										.toString()));
			}

			if (con.getRequestProperty("Accept-Encoding") == null) {
				con.setRequestProperty("Accept-Encoding", "gzip");
			}

			if (requestBody != null && !requestBody.equals("")) {
				con.setDoOutput(true);
				DataOutputStream wr = new DataOutputStream(
						con.getOutputStream());
				wr.write(Utility.toUtf8Bytes(requestBody));
				wr.flush();
				wr.close();

			}

			// push response
			BufferedReader in = null;
			String inputLine;

			List<String> contentEncoding = con.getHeaderFields().get(
					"Content-Encoding");
			if (contentEncoding != null) {
				for (String val : contentEncoding) {
					if ("gzip".equalsIgnoreCase(val)) {
						sLog.debug("Gzip enabled response");
						gzip = true;
						break;
					}
				}
			}

			sLog.debug("Response: '{} {}' with headers '{}'",
					con.getResponseCode(), con.getResponseMessage(),
					ServletUtil.buildHeadersForLog(con.getHeaderFields()));

			if (con.getResponseCode() != 200 && con.getResponseCode() != 201) {
				if (con.getErrorStream() != null) {
					if (gzip) {
						in = new BufferedReader(new InputStreamReader(
								new GZIPInputStream(con.getErrorStream()),
								"UTF-8"));
					} else {
						in = new BufferedReader(new InputStreamReader(
								con.getErrorStream(), "UTF-8"));
					}
				}
			} else {
				String[] urlParts = url.toString().split("\\.");
				if (urlParts.length > 1) {
					String ext = urlParts[urlParts.length - 1];
					if (ext.equalsIgnoreCase("png")
							|| ext.equalsIgnoreCase("jpg")
							|| ext.equalsIgnoreCase("jpeg")
							|| ext.equalsIgnoreCase("gif")) {
						BufferedImage imBuff;
						if (gzip) {
							imBuff = ImageIO.read(new GZIPInputStream(con
									.getInputStream()));
						} else {
							BufferedInputStream bfs = new BufferedInputStream(con.getInputStream());		
							imBuff = ImageIO.read(bfs);
						}
						BufferedImage newImage = new BufferedImage(
								imBuff.getWidth(), imBuff.getHeight(),
								BufferedImage.TYPE_3BYTE_BGR);

						// converting image to greyScale
						int width = imBuff.getWidth();
						int height = imBuff.getHeight();
						for (int i = 0; i < height; i++) {
							for (int j = 0; j < width; j++) {
								Color c = new Color(imBuff.getRGB(j, i));
								int red = (int) (c.getRed() * 0.21);
								int green = (int) (c.getGreen() * 0.72);
								int blue = (int) (c.getBlue() * 0.07);
								int sum = red + green + blue;
								Color newColor = new Color(sum, sum, sum);
								newImage.setRGB(j, i, newColor.getRGB());
							}
						}

						ByteArrayOutputStream out = new ByteArrayOutputStream();
						ImageIO.write(newImage, "jpg", out);
						byte[] bytes = out.toByteArray();

						byte[] encodedBytes = Base64.encodeBase64(bytes);
						String base64Src=new String(encodedBytes);
						int imageSize =((base64Src.length()*3)/4)/1024;
						int initialImageSize = imageSize;
						int maxImageSize = Integer.parseInt(properties.getValue("Reduced_Image_Size"));
						float quality=0.9f;
						if(!(imageSize<=maxImageSize)){
							//This means that image size is greater and needs to be reduced.
							sLog.debug("Image size is greater than "+maxImageSize+" , compressing image.");
							while(!(imageSize<maxImageSize)){
								 base64Src= compress(base64Src, quality);
								 imageSize =((base64Src.length()*3)/4)/1024;
								quality=quality-0.1f;
								DecimalFormat df = new DecimalFormat("#.0");
								quality=Float.parseFloat(df.format(quality));
								if(quality<=0.1){
									break;
								}
							}
						}
						sLog.debug("Initial image size was : "+initialImageSize +" Final Image size is : "+imageSize+"Url is : "+url+"quality is :"+quality);
						String src = "data:image/"+urlParts[urlParts.length-1]+";base64," + new String(base64Src);
						JSONObject joResult = new JSONObject();
						joResult.put("Image", src);
						out.close();
						httpResult.setResponseCode(con.getResponseCode());
						httpResult.setResponseHeader(con.getHeaderFields());
						httpResult.setResponseBody(joResult.toString());
						httpResult.setResponseMsg(con.getResponseMessage());
						return httpResult;
					}
				}

				if (gzip) {
					in = new BufferedReader(new InputStreamReader(
							new GZIPInputStream(con.getInputStream()), "UTF-8"));
				} else {
					in = new BufferedReader(new InputStreamReader(
							con.getInputStream(), "UTF-8"));
				}
			}
			if (in != null) {
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			}

			httpResult.setResponseCode(con.getResponseCode());
			httpResult.setResponseHeader(con.getHeaderFields());
			httpResult.setResponseBody(response.toString());
			httpResult.setResponseMsg(con.getResponseMessage());

		} catch (Exception e) {
			sLog.error("Failed to received HTTP response after timeout", e);

			

			httpResult.setTimeout(true);
			httpResult.setResponseCode(500);
			httpResult.setResponseMsg("Internal Server Error Timeout");
			return httpResult;
		} 

		
		return httpResult;
	}
	
	public static String compress(String imageString, float quality) throws IOException{
		byte[] imageByte=Base64.decodeBase64(imageString);
		ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
		BufferedImage image = ImageIO.read(bis);
	    bis.close();
	    
	    // Get a ImageWriter for given extension format.
		
	    Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("jpg");
	    if (!writers.hasNext()) throw new IllegalStateException("No writers found");
	    
	    ImageWriter writer = (ImageWriter) writers.next();
	    // Create the ImageWriteParam to compress the image.
	    ImageWriteParam param = writer.getDefaultWriteParam();
	    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
	    param.setCompressionQuality(quality);
	    
	    // The output will be a ByteArrayOutputStream (in memory)
	    ByteArrayOutputStream bos = new ByteArrayOutputStream(32768);
	    ImageOutputStream ios = ImageIO.createImageOutputStream(bos);
	    writer.setOutput(ios);
	    writer.write(null, new IIOImage(image, null, null), param);
	    ios.flush(); // otherwise the buffer size will be zero!
	    byte[] encodedBytes = Base64.encodeBase64( bos.toByteArray());
	    
	    return new String(encodedBytes);
	    }
	
	private static void logExtAccess(final String type, final URL url, final String method, final int responseStatus,
			final int responseBytes, final long takenMs) {
		LogUtil.logExtAccess(url.getHost(), type, method, responseStatus, responseBytes, takenMs, 
				ServletUtil.filterUrl(url.getPath() + (url.getQuery() == null ? "" : "?" + url.getQuery())));
	}
	
	private int getQueryCost(HttpServletRequest request){
		int multiplier = 1;
		if(request != null){
			String reqAction = request.getMethod();
			final String url = request.getRequestURI().toString();
			String doubleReadTimeout = properties.getValue("doubleReadTimeout");
			String tripleReadTimeout = properties.getValue("tripleReadTimeout");
			
			if("GET".equals(reqAction.toUpperCase()) && (url.contains("/metrics")|| url.contains("/tops"))){
				Long startTime = Utility.formatDate(request.getParameter("startTime"));
				Long endTime = Utility.formatDate(request.getParameter("endTime"));
				Long diff = (endTime-startTime)/(3600000*24); // diff in days
				if(diff >=1 && diff<7)
					multiplier = 2;
				else if(diff >=7)
					multiplier=3;
			}else {
				if(matchPattern(url, doubleReadTimeout, reqAction))
					multiplier=2;
				else if(matchPattern(url, tripleReadTimeout, reqAction))
					multiplier=3;
			}
		}

		return multiplier;
	}

	private Boolean matchPattern(final String url, String multiplierStr, String method) {
		JSONArray jsonArr = (JSONArray)Utility.parse(multiplierStr);
		for(int i =0; i <jsonArr.size(); i++){
			JSONObject obj = (JSONObject) jsonArr.get(i);
			String regex = obj.get("path").toString();
			String methodStr = obj.get("method").toString();
			Pattern pattern = Pattern.compile(regex); 
			Matcher match = pattern.matcher(url);
			boolean found= match.find();
			if( found && methodStr.contains(method)){
				return (found && methodStr.contains(method));
			}
		}
		return false;
	}
}

package com.officedrop.samples;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * Created by IntelliJ IDEA.
 * User: mauricio
 * Date: 09/11/11
 * Time: 20:18
 * To change this template use File | Settings | File Templates.
 */
public class DownloadSample {

    public static void main(String... args) throws Exception {

        String username = "your-username-here";
        String password = "your-password-here";

        String authorization = Base64.encodeBase64String(String.format("%s:%s", username, password).getBytes()).trim();

        HttpParams params = new BasicHttpParams();
        params.setBooleanParameter( "http.protocol.handle-redirects", false );

        HttpGet request = new HttpGet(new URI("https://www.officedrop.com/ze/api/documents/637994/original"));
        request.setParams(params);
        request.addHeader("Authorization", String.format("Basic %s", authorization));

        HttpHost host = new HttpHost(request.getURI().getHost(), request
                .getURI().getPort(), request.getURI().getScheme());

        HttpClient client = new DefaultHttpClient();

        HttpResponse response = client.execute(host, request);
        dumpResponse(response);
        System.out.printf("Content is:%n%s%n", EntityUtils.toString(response.getEntity()));

        Header location = response.getLastHeader("Location");

        HttpGet finalRequest = new HttpGet( new URI( location.getValue() ) );

        System.out.println();

        HttpResponse finalResponse = client.execute( finalRequest );
        dumpResponse(finalResponse);

        String disposition = null;

        for (Header header : finalResponse.getAllHeaders() ) {

            if ( "Content-Disposition".equals( header.getName() ) ) {
                disposition = header.getValue();
            }

        }

        String filename = disposition.substring( disposition.indexOf("=") + 1 );

        System.out.println( "Writting to file " + filename );

        OutputStream out = new BufferedOutputStream(  new FileOutputStream( filename ) );
        finalResponse.getEntity().writeTo( out );
        finalResponse.getEntity().getContent().close();
        out.flush();
        out.close();

        System.out.println( "Finished writting to file" );


    }

    public static void dumpResponse( HttpResponse response ) {

        System.out.printf("Response - %d - %s - %n", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());

        for (Header header : response.getAllHeaders()) {
            System.out.printf("[%s] -> [%s]%n", header.getName(), header.getValue());
        }

    }


}

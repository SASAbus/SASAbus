/**
 *
 * SasabusFTP.java
 * 
 * Created: Feb 2, 2012 16:20:40 PM
 * 
 * Copyright (C) 2012 Markus Windegger
 * 
 *
 * This file is part of SasaBus.

 * SasaBus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SasaBus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SasaBus.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */


package it.sasabz.android.sasabus.classes.network;




import it.sasabz.android.sasabus.classes.services.FileRetriever;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.StringTokenizer;

import android.util.Log;




public class SasabusFTP {
	
	private static int TIMEOUT_FILETRANSFER_MS = 5000;
    
	 private Socket socket = null;
	 private BufferedReader reader = null;
	 private BufferedWriter writer = null;
	 
	 private boolean connected = false;
	 
	 private boolean login = false;
	    
	 private static boolean DEBUG = false;
    
    
	 /**
	 * @return the connected
	 */
	public synchronized boolean isConnected() {
		return connected;
	}


	/**
	 * @return the login
	 */
	public synchronized boolean isLogin() {
		return login;
	}

	 
	 /**
     * Connects to the default port of an FTP server
     * @throws IOEcxeption if something goes wrong
     */
    public synchronized void connect(String host) throws IOException {
        connect(host, 21);
    }
    
    
    /**
     * Connects to an FTP server
     * @param host is the host to connect with
     * @param port is the port to connect with
     * @throws IOEcxeption if something goes wrong
     */
    public synchronized void connect(String host, int port) throws IOException {
    	if (socket != null) {
            throw new IOException("SimpleFTP is already connected. Disconnect first.");
        }
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
           
        String response = readLine();
        if (!response.startsWith("220 ")) {
            throw new IOException("SimpleFTP received an unknown response when connecting to the FTP server: " + response);
        }
        connected = true;
    }
    
    
    /**
     * Connects to an FTP server and logs in with the supplied username
     * and password.
     * @param host is the hostname to connect with
     * @param port is the port to connect with
     * @param user is the username to use when doing the login
     * @param pass is the password to login the user
     * @throws IOException if something goes wrong
     */
    public synchronized void connect(String host, int port, String user, String pass) throws IOException {
       connect(host, port);
       login(user, pass);
    }
    
    
    /**
     * Logs in with the supplied username and password to a cpnnected server
     * @param user the user to log in with
     * @param pass the password to login with
     * @throws IOException if something goes wrong
     */
    public synchronized void login(String user, String pass) throws IOException{
    	if(!connected)
    	{
    		throw new IOException("SimpleFTP is disconnected. Connect first to any host");
    	}
    	String response;
    	
    	sendLine("USER " + user);
        
        response = readLine();
        if (!response.startsWith("331 ")) {
            throw new IOException("SimpleFTP received an unknown response after sending the user: " + response);
        }
        
        sendLine("PASS " + pass);
        
        response = readLine();
        if (!response.startsWith("230 ")) {
            throw new IOException("SimpleFTP was unable to log in with the supplied password: " + response);
        }
        login = true;
    }
    
    /**
     * Disconnects from the FTP server.
     * @throws IOEcxeption if something goes wrong
     */
    public synchronized void disconnect() throws IOException {
        try {
            sendLine("QUIT");
        }
        finally {
            socket = null;
            connected = false;
            login = false;
        }
    }
    
    
    /**
     * Returns the working directory of the FTP server it is connected to.
     * @throws IOEcxeption if something goes wrong
     */
    public synchronized String pwd() throws IOException {
    	if(!connected)
    	{
    		throw new IOException("Server not connected");
    	}
    	if(!login)
    	{
    		throw new IOException("Not logged in");
    	}
        sendLine("PWD");
        String dir = null;
        String response = readLine();
        if (response.startsWith("257 ")) {
            int firstQuote = response.indexOf('\"');
            int secondQuote = response.indexOf('\"', firstQuote + 1);
            if (secondQuote > 0) {
                dir = response.substring(firstQuote + 1, secondQuote);
            }
        }
        return dir;
    }
    
    /**
     * Returns a boolean wich says you if the file exists or not
     * @param filename is the filename of the file to search for
     * @return a boolean if the file filename exists or not
     * @throws IOException if something goes wrong
     */
    public synchronized boolean exists(String filename) throws IOException {
    	if(!connected)
    	{
    		throw new IOException("Server not connected");
    	}
    	if(!login)
    	{
    		throw new IOException("Not logged in");
    	}
        sendLine("SIZE " + filename);
        String response = readLine();
        return (!response.startsWith("550 "));
    }
    
    
    /**
     * Changes permission to remote file   
     * @param perms are the permissions (oktal, e.g. 755) for the file 
     * @param file is the remote filename for what we choose the permissions
     * @return a boolean if the operation wos successful or not
     * @throws IOException when something goes wrong
     */
    public synchronized boolean chmod(String perms, String file) throws IOException {
    	if(!connected)
    	{
    		throw new IOException("Server not connected");
    	}
    	if(!login)
    	{
    		throw new IOException("Not logged in");
    	}
        sendLine("SITE CHMOD " + perms + " " + file);
        String response = readLine();
        Log.v("simpleftp-logger", "chmod response: " + response);
        return (response.startsWith("200 "));
    }


   
    /**
     * Changes the working directory (like cd). Returns true if successful.
     * @param dir is the dir to change in
     * @return a boolean if the operation was succesful or not
     * @throws IOException if something goes wrong
     */
    public synchronized boolean cwd(String dir) throws IOException {
    	if(!connected)
    	{
    		throw new IOException("Server not connected");
    	}
    	if(!login)
    	{
    		throw new IOException("Not logged in");
    	}
        sendLine("CWD " + dir);
        String response = readLine();
        Log.v("simpleftp=logger","cwd response: " + response);
        return (response.startsWith("250 "));
    }
    
    

    /**
     * Sends a file to be stored on the FTP server.
     * The file is sent in passive mode to avoid NAT or firewall problems
     * at the client end.
     * @param file is the file to send to the server
     * @return true if the file transfer was successful.
     * @throws IOException if something goes wrong
     */
    public synchronized boolean stor(File file) throws IOException {
    	if(!connected)
    	{
    		throw new IOException("Server not connected");
    	}
    	if(!login)
    	{
    		throw new IOException("Not logged in");
    	}
        if (file.isDirectory()) {
            throw new IOException("SimpleFTP cannot upload a directory.");
        }
        
        String filename = file.getName();

        return stor(new FileInputStream(file), filename);
    }
    

    
    /**
     * Sends a file to be stored on the FTP server.
     * The file is sent in passive mode to avoid NAT or firewall problems
     * at the client end.
     * @param inputStream is the input stream to send to the server
     * @param filename is the filname of the remote file
     * @return true if the filetransfer was successful
     * @throws IOException if something goes wrong
     */
    public synchronized boolean stor(InputStream inputStream, String filename) throws IOException {
    	if(!connected)
    	{
    		throw new IOException("Server not connected");
    	}
    	if(!login)
    	{
    		throw new IOException("Not logged in");
    	}
        BufferedInputStream input = new BufferedInputStream(inputStream);
        
        sendLine("PASV");
        String response = readLine();
        if (!response.startsWith("227 ")) {
            throw new IOException("SimpleFTP could not request passive mode: " + response);
        }
        Log.v("simpleftp-logger", response);
        String ip = null;
        int port = -1;
        int opening = response.indexOf('(');
        int closing = response.indexOf(')', opening + 1);
        if (closing > 0) {
            String dataLink = response.substring(opening + 1, closing);
            Log.v("simpleftp-logger", dataLink);
            StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
            try {
                ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken();
                port = Integer.parseInt(tokenizer.nextToken()) * 256 + Integer.parseInt(tokenizer.nextToken());
                Log.v("simpleftp-logger", "IP: " + ip + " | PORT: " + port);
            }
            catch (Exception e) {
                throw new IOException("SimpleFTP received bad data link information: " + response);
            }
        }
        
        sendLine("STOR " + filename);
        
        Socket dataSocket = new Socket(ip, port);
        
        response = readLine();
        if (!response.startsWith("150 ")) {
            throw new IOException("SimpleFTP was not allowed to send the file: " + response);
        }
        
        BufferedOutputStream output = new BufferedOutputStream(dataSocket.getOutputStream());
        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        output.flush();
        output.close();
        input.close();
        
        response = readLine();
        Log.v("simpleftp", "stor response: " + response);
        return response.startsWith("226 ");
    }


    
    /**
     * 
     * @param outputStream is the fileoutputstream to store the file from the server locally
     * @param filename is the filename of the remote file
     * @return true if the file transfer was successful
     * @throws Exception 
     */
    public synchronized boolean get(FileOutputStream outputStream, String filename, FileRetriever fileret) throws Exception
    {
    	if(!connected)
    	{
    		throw new IOException("Server not connected");
    	}
    	if(!login)
    	{
    		throw new IOException("Not logged in");
    	}
    	
    	int lenghtOfFile = this.size(filename);
    	BufferedOutputStream output = new BufferedOutputStream(outputStream);
        
        sendLine("PASV");
        String response = readLine();
        if (!response.startsWith("227 ")) {
            throw new IOException("SimpleFTP could not request passive mode: " + response);
        }
        Log.v("simpleftp-logger", response);
        String ip = null;
        int port = -1;
        int opening = response.indexOf('(');
        int closing = response.indexOf(')', opening + 1);
        if (closing > 0) {
            String dataLink = response.substring(opening + 1, closing);
            Log.v("simpleftp-logger", dataLink);
            StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
            try {
                ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken();
                port = Integer.parseInt(tokenizer.nextToken()) * 256 + Integer.parseInt(tokenizer.nextToken());
                Log.v("simpleftp-logger", "IP: " + ip + " | PORT: " + port);
            }
            catch (Exception e) {
                throw new IOException("SimpleFTP received bad data link information: " + response);
            }
        }
        
        sendLine("RETR " + filename);
        
        Socket dataSocket = new Socket(ip, port);
        
        response = readLine();
        if (!response.startsWith("150 ")) {
            throw new IOException("SimpleFTP was not allowed to retrieve the file: " + response);
        }
        dataSocket.setSoTimeout(TIMEOUT_FILETRANSFER_MS);
        BufferedInputStream input = new BufferedInputStream(dataSocket.getInputStream());
        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        long total = 0;
        try
        {	
	        while ((bytesRead = input.read(buffer)) != -1) {
	            output.write(buffer, 0, bytesRead);
	            total += bytesRead;
	            fileret.publishProgress("" + (int) (total * 100 / lenghtOfFile));
	        }
        }
        catch(Exception e)
        {
        	input.close();
        	dataSocket.close();
        	output.flush();
        	output.close();
        	throw e;
        }
        output.flush();
        output.close();
        input.close();
        
        response = readLine();
        Log.v("simpleftp", "retr response: " + response);
        return response.startsWith("226 ");
    }
    
    /**
     * Send a size request to the ftp-host
     * @param filename is the name of the file of which request the filesize
     * @return the size of the file in bytes
     * @throws IOException if the host is not connected or not logged in
     */
    public synchronized int size(String filename) throws IOException
    {
    	if(!connected)
    	{
    		throw new IOException("Server not connected");
    	}
    	if(!login)
    	{
    		throw new IOException("Not logged in");
    	}
    	 sendLine("SIZE " + filename);
         String response = readLine();
         if(!response.startsWith("213 "))
         {
        	 throw new IOException("Failure when requesting the size. response: " + response);
         }
         return Integer.parseInt(response.substring(4));
    }
    
    /**
     * Provides the last remote date
     * @return the modification time
     * @throws IOException if something goes wrong
     */
    public synchronized String getModificationTime(String filename) throws IOException
    {
    	if(!connected)
    	{
    		throw new IOException("Server not connected");
    	}
    	if(!login)
    	{
    		throw new IOException("Not logged in");
    	}
    	 sendLine("MDTM " + filename);
    	 Log.v("FTP", "SEND: MDTM " + filename);
         String response = readLine();
         Log.v("FTP", "RESPONSE : " + response);
         if(!response.startsWith("213 "))
         {
        	 throw new IOException("Failure when requesting the last modification time. response: " + response);
         }
         return response.substring(4);
    }
    
    
    /**
     * Enter binary mode for sending binary files
     * @return true if the mode was successful changed
     * @throws IOException if something goes wrong
     */
    public synchronized boolean bin() throws IOException {
    	if(!connected)
    	{
    		throw new IOException("Server not connected");
    	}
    	if(!login)
    	{
    		throw new IOException("Not logged in");
    	}
        sendLine("TYPE I");
        String response = readLine();
        return (response.startsWith("200 "));
    }
    


    /**
     * Enter ASCII mode for sending text files. This is usually the default
     * mode. Make sure you use binary mode if you are sending images or
     * other binary data, as ASCII mode is likely to corrupt them.
     * @return true if mode was successfully changed
     * @throws IOException if something goes wrong
     */
    public synchronized boolean ascii() throws IOException {
    	if(!connected)
    	{
    		throw new IOException("Server not connected");
    	}
    	if(!login)
    	{
    		throw new IOException("Not logged in");
    	}
        sendLine("TYPE A");
        String response = readLine();
        return (response.startsWith("200 "));
    }
    
    
    
    /**
     * Sends a raw command to the FTP server via server socket
     * @param line is the command to send
     * @throws IOException if something goes wrong
     */
    private void sendLine(String line) throws IOException {
        if (socket == null) {
            throw new IOException("SimpleFTP is not connected.");
        }
        try {
            writer.write(line + "\r\n");
            writer.flush();
            if (DEBUG) {
                System.out.println("> " + line);
            }
        }
        catch (IOException e) {
            socket = null;
            throw e;
        }
    }
    
    
    /**
     * Reads a line from the server socket
     * @return the read line
     * @throws IOException if something goes wrong
     */
    private String readLine() throws IOException {
        String line = reader.readLine();
        if (DEBUG) {
            System.out.println("< " + line);
        }
        return line;
    }
    
    
}
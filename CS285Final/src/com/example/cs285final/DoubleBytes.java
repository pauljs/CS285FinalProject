package com.example.cs285final;


public class DoubleBytes {
	
	private byte[] ciphertext;
	private byte[] encodedParams;

	public DoubleBytes(byte[] ciphertext, byte[] encodedParams) {
		// TODO Auto-generated constructor stub
		this.setCiphertext(ciphertext);
		this.setEncodedParams(encodedParams);
	}

	public byte[] getEncodedParams() {
		return encodedParams;
	}

	private void setEncodedParams(byte[] encodedParams) {
		this.encodedParams = encodedParams;
	}

	public byte[] getCiphertext() {
		return ciphertext;
	}

	private void setCiphertext(byte[] ciphertext) {
		this.ciphertext = ciphertext;
	}

	
}
package com.example.cs285final;

import java.security.KeyPair;

import javax.crypto.KeyAgreement;

public class Transfer {
	
	private KeyPair aliceKpair;
	private KeyAgreement aliceKeyAgree;

	public Transfer(KeyPair aliceKpair, KeyAgreement aliceKeyAgree) {
		// TODO Auto-generated constructor stub
		this.aliceKpair = aliceKpair;
		this.aliceKeyAgree = aliceKeyAgree;
	}

	public KeyPair getAliceKpair() {
		return aliceKpair;
	}

	public KeyAgreement getAliceKeyAgree() {
		return aliceKeyAgree;
	}
}

package com.asobancaria.tciweb1.cifin.confrontav2plusws.services.ConfrontaUltraWS;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.message.WSSecSignature;
import sun.security.rsa.SunRsaSign;

import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import java.security.MessageDigest;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Signature;

public class MyWSSecSignature extends WSSecSignature {
    public MyWSSecSignature(){
        super();
    }
}

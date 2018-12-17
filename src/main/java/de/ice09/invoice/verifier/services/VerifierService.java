package de.ice09.invoice.verifier.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;

public class VerifierService {

    private static Log log = LogFactory.getLog(VerifierService.class);

    public String ecrecoverAddress(byte[] proof, byte[] r, byte[] s, String expectedAddress) {
        ECDSASignature esig = new ECDSASignature(Numeric.toBigInt(r), Numeric.toBigInt(s));
        BigInteger res;
        for (int i=0; i<4; i++) {
            res = Sign.recoverFromSignature(i, esig, proof);
            String addr = Keys.getAddress(res).toLowerCase();
            if ((res != null) && addr.equals(expectedAddress.substring(2).toLowerCase())) {
                log.info("public Ethereum address: 0x" + Keys.getAddress(res));
                return Keys.getAddress(res);
            }
        }
        return null;
    }
}

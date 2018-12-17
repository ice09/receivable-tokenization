package de.ice09.invoice.verifier.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.web3j.crypto.*;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class SignatureService {

    private static Log log = LogFactory.getLog(SignatureService.class);
    private final Credentials credentials;

    public SignatureService() {
        String privateKey1 = "c87509a1c067bbde78beb793e6fa76530b6382a4c0241e5e4a9ec0a0f44dc0d3";
        credentials = Credentials.create(privateKey1);
        log.info("Address: " + credentials.getAddress());
    }

    public String sign(String id, String buyer, String seller, String duedate, BigInteger total) throws Exception {
        String proofStr = createEIP712Proof(id, buyer, seller, duedate, total);

        // sign
        Sign.SignatureData signature = Sign.signMessage(Numeric.hexStringToByteArray(proofStr), credentials.getEcKeyPair(), false);
        ByteBuffer sigBuffer = ByteBuffer.allocate(signature.getR().length + signature.getS().length + 1);
        sigBuffer.put(signature.getR());
        sigBuffer.put(signature.getS());
        sigBuffer.put(signature.getV());

        log.info(String.format("signed proof: %s", Numeric.toHexString(sigBuffer.array())));
        return Numeric.toHexStringNoPrefix(sigBuffer.array());
    }

    public String createEIP712Proof(String id, String seller, String buyer, String duedate, BigInteger total) {
        byte[] domainSep = Hash.sha3("EIP712Domain(string name,string version,uint256 chainId,address verifyingContract,bytes32 salt)".getBytes());
        byte[] akycType = Hash.sha3("Ard(string id,address seller,address buyer,string duedate,uint256 total)".getBytes());
        String domainAsString = Numeric.toHexString(domainSep) +
                Numeric.toHexString(Hash.sha3("Account Receivable Signer".getBytes())).substring(2) +
                Numeric.toHexString(Hash.sha3("1".getBytes())).substring(2) +
                Numeric.toHexStringNoPrefix(Numeric.toBytesPadded(BigInteger.valueOf(5777), 32)) +
                "0000000000000000000000001C56346CD2A2Bf3202F771f50d3D14a367B48070" +
                "f2d857f4a3edcb9b78b4d503bfe733db1e3f6cdc2b7971ee739626c97e86a558";
        String proofStr = Numeric.toHexStringNoPrefix(Hash.sha3(
                Numeric.hexStringToByteArray(
                        "0x1901" +
                        Numeric.toHexStringNoPrefix(Hash.sha3(Numeric.hexStringToByteArray(domainAsString))) +
                        Numeric.toHexStringNoPrefix(Hash.sha3(Numeric.hexStringToByteArray(
                                Numeric.toHexStringNoPrefix(akycType) +
                                Numeric.toHexStringNoPrefix(Hash.sha3(id.getBytes())) +
                                "000000000000000000000000" + seller.toLowerCase().substring(2) +
                                "000000000000000000000000" + buyer.toLowerCase().substring(2) +
                                Numeric.toHexStringNoPrefix(Hash.sha3(duedate.getBytes())) +
                                Numeric.toHexStringNoPrefix(Numeric.toBytesPadded(total, 32)))
                        )))));
        log.info("proof plain:" + proofStr);
        return proofStr;
    }

}

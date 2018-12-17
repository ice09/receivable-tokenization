package de.ice09.invoice.verifier.controller;

import de.ice09.invoice.verifier.Verifier;
import de.ice09.invoice.verifier.services.*;
import io.ipfs.multihash.Multihash;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

@RestController
public class VerifierController {

    private final Verifier verifier;
    private final Credentials credentials;
    private TokenService tokenService = new TokenService();
    private SignatureService signatureService = new SignatureService();
    private VerifierService verifierService = new VerifierService();
    private final Web3j web3;

    private static Log log = LogFactory.getLog(VerifierController.class);

    public VerifierController(Web3j web3) throws Exception {
        this.web3 = web3;
        String privateKey1 = "c87509a1c067bbde78beb793e6fa76530b6382a4c0241e5e4a9ec0a0f44dc0d3";
        credentials = Credentials.create(privateKey1);
        verifier = Verifier.deploy(web3, credentials, ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT).send();
    }

    @RequestMapping("/verify")
    public String verify(
            @RequestParam(value="arg") String arg) throws Exception {
        Map<String, String> map = Input.parse(arg);
        String id = map.get("id");
        String duedate = map.get("duedate");
        BigInteger total = new BigInteger(map.get("total"));
        String buyer = map.get("buyer");
        String seller = map.get("seller");
        String rS = map.get("sigR");
        String sS = map.get("sigS");
        BigInteger v = new BigInteger(map.get("sigV"));
        byte[] r = Numeric.hexStringToByteArray(rS);
        byte[] s = Numeric.hexStringToByteArray(sS);

        String signer = verifier.verify(id, seller, buyer, duedate, total, r, s, v).send();
        String signerJava = verifierService.ecrecoverAddress(
                Numeric.hexStringToByteArray(signatureService.createEIP712Proof(id, seller, buyer, duedate, total)),
                r, s, signer);
        log.info(signerJava);
        TokenService.Erc20Dto erc20Dto = tokenService.deployErc20(web3, credentials, total);

        Multihash hash = DStore.storeFile(erc20Dto.getAddr(), erc20Dto.getSupply());
        log.info("IPFS Multihash:" + hash.toBase58());
        DStore.readFile(hash);

        String erc721addr = tokenService.deployErc721(web3, credentials, hash);
        return "<b>Signer:</b> " + signer + "<br/><b>IPFS Multihash:</b> " + hash.toBase58() + "<br/><b>ERC 20:</b> " + erc20Dto.getAddr() + "<br/><b>Supply:</b> " + erc20Dto.getSupply() + "<br/><b>ERC 721 NFT:</b> " + erc721addr;
    }
}

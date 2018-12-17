package de.ice09.invoice.verifier.services;

import de.ice09.invoice.verifier.Erc20Token;
import de.ice09.invoice.verifier.Token;
import io.ipfs.multihash.Multihash;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

import java.math.BigInteger;

public class TokenService {

    private static Log log = LogFactory.getLog(TokenService.class);

    public Erc20Dto deployErc20(Web3j web3j, Credentials credentials, BigInteger total) throws Exception {
        Erc20Token erc20 = Erc20Token.deploy(web3j, credentials, ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT).send();
        erc20.mint(credentials.getAddress(), total).send();
        return new Erc20Dto(erc20.getContractAddress(), erc20.totalSupply().send());
    }

    public String deployErc721(Web3j web3j, Credentials credentials, Multihash hash) throws Exception {
        Token erc721 = Token.deploy(web3j, credentials, ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT, new BigInteger("1000000000000000000")).send();
        log.info(erc721.mint(credentials.getAddress(), BigInteger.ONE).send().toString());
        erc721.setTokenURI(BigInteger.ONE, hash.toBase58()).send();
        log.info("exists?" + erc721.balanceOf(credentials.getAddress()).send());
        log.info(erc721.totalSupply().send().toString());
        log.info("uri:" + erc721.tokenURI(BigInteger.ONE).send());
        return erc721.getContractAddress();
    }

    public static class Erc20Dto {
        private String addr;
        private BigInteger supply;

        public Erc20Dto(String addr, BigInteger supply) {
            this.addr = addr;
            this.supply = supply;
        }

        public String getAddr() {
            return addr;
        }

        public BigInteger getSupply() {
            return supply;
        }

    }

}

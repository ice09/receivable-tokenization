package de.ice09.invoice.verifier.services;

import com.google.gson.Gson;
import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DStore {

    private static final Logger log = LoggerFactory.getLogger(DStore.class);

    public static Multihash storeFile(String addr, BigInteger supply) throws IOException {
        Map<String, Object> ard = new HashMap<>();
        String json = Files.lines(new File("src/main/resources/invoice.json").toPath()).collect(Collectors.joining());
        json = json.replaceAll("<ERC20>", addr)
                .replaceAll("\"<SUPPLY>\"", supply.toString());
        IPFS ipfs = new IPFS("/ip4/127.0.0.1/tcp/5001");
        NamedStreamable.ByteArrayWrapper nsb = new NamedStreamable.ByteArrayWrapper(json.getBytes("UTF-8"));
        MerkleNode merkleNode = ipfs.add(nsb).get(0);
        return merkleNode.hash;
    }

    public static void readFile(Multihash multihash) throws IOException {
        IPFS ipfs = new IPFS("/ip4/127.0.0.1/tcp/5001");
        byte[] content = ipfs.cat(multihash);
        String sCnt = new String(content, "UTF-8");
        log.info(sCnt);
        Map jsonJavaRootObject = new Gson().fromJson(sCnt, Map.class);
        log.info(jsonJavaRootObject.get("BASE64").toString());
        byte[] decodedImg = Base64.getDecoder().decode(jsonJavaRootObject.get("BASE64").toString().getBytes(StandardCharsets.UTF_8));
        Path destinationFile = Paths.get("src/main/resources", "inv.pdf");
        Files.write(destinationFile, decodedImg);
    }

}

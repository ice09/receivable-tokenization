function parseSignature(signature) {
  var r = signature.substring(0, 64);
  var s = signature.substring(64, 128);
  var v = signature.substring(128, 130);

  return {
      r: "0x" + r,
      s: "0x" + s,
      v: parseInt(v, 16)
  }
}

function genCsv(signature, signer, ard) {
	return csv.replace("<SIGR>", signature.r)
	  .replace("<SIGS>", signature.s)
    .replace("<SIGV>", signature.v)
    .replace("<SIGNER>", signer)
    .replace("<ID>", ard.id)
    .replace("<SELLER>", ard.seller)
    .replace("<BUYER>", ard.buyer)
    .replace("<DUEDATE>", ard.duedate)
    .replace("<TOTAL>", ard.total)
}

window.onload = function (e) {
  var res = document.getElementById("resdiv");
  var inv = document.getElementById("inv");
  res.style.display = "none";
  inv.style.display = "block";
  // force the user to unlock their MetaMask
  if (web3.eth.accounts[0] == null) {
    alert("Please unlock MetaMask first");
  }

  var signBtn = document.getElementById("signBtn");
  signBtn.onclick = function(e) {
    if (web3.eth.accounts[0] == null) {
      return;
    }

    const domain = [
      { name: "name", type: "string" },
      { name: "version", type: "string" },
      { name: "chainId", type: "uint256" },
      { name: "verifyingContract", type: "address" },
      { name: "salt", type: "bytes32" },
    ];

    const ard = [
      { name: "id", type: "string" },
      { name: "seller", type: "address" },
      { name: "buyer", type: "address" },
      { name: "duedate", type: "string" },
      { name: "total", type: "uint256" },
    ];

    const domainData = {
      name: "Account Receivable Signer",
      version: "1",
      chainId: 5777,
      verifyingContract: "0x1C56346CD2A2Bf3202F771f50d3D14a367B48070",
      salt: "0xf2d857f4a3edcb9b78b4d503bfe733db1e3f6cdc2b7971ee739626c97e86a558"
    };

    var message = {
      id: document.getElementById("id").value,
      seller: document.getElementById("seller").value,
      buyer: document.getElementById("buyer").value,
      duedate: document.getElementById("due").value,
      total: document.getElementById("total").value
    };
    console.log(message);

    //"0xf2d857f4a3edcb9b78b4d503bfe733db1e3f6cdc2b7971ee739626c97e86a558","0x9c019724e411F546d3AC6D42861A5e7D81C0f497","0x1C56346CD2A2Bf3202F771f50d3D14a367B48070","01.04.2100","1000000"

    data = JSON.stringify({
      types: {
        EIP712Domain: domain,
        Ard: ard
      },
      domain: domainData,
      primaryType: "Ard",
      message: message
    });

    const signer = web3.eth.accounts[0];
    console.log(data);
    //data='{"types":{"EIP712Domain":[{"name":"name","type":"string"},{"name":"version","type":"string"},{"name":"chainId","type":"uint256"},{"name":"verifyingContract","type":"address"},{"name":"salt","type":"bytes32"}],"Bid":[{"name":"amount","type":"uint256"},{"name":"bidder","type":"Identity"}],"Identity":[{"name":"userId","type":"uint256"},{"name":"wallet","type":"address"}]},"domain":{"name":"My amazing dApp","version":"2","chainId":1,"verifyingContract":"0x1C56346CD2A2Bf3202F771f50d3D14a367B48070","salt":"0xf2d857f4a3edcb9b78b4d503bfe733db1e3f6cdc2b7971ee739626c97e86a558"},"primaryType":"Bid","message":{"amount":100,"bidder":{"userId":323,"wallet":"0x3333333333333333333333333333333333333333"}}}';

    web3.currentProvider.sendAsync(
      {
        method: "eth_signTypedData_v3",
        params: [signer, data],
        from: signer
      },
      function(err, result) {
        if (err || result.error) {
          return console.error(result);
        }

        const signature = parseSignature(result.result.substring(2));

        res.style.display = "block";
        inv.style.display = "none";

        $.get("/verify?arg=" + genCsv(signature, signer, message), function(data) {
                document.getElementById("response").innerHTML = data;
            });

      }
    );
  };
}
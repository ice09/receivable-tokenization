pragma experimental ABIEncoderV2;
pragma solidity ^0.4.23;

contract Verifier {
    uint256 constant chainId = 5777;
    address constant verifyingContract = 0x1C56346CD2A2Bf3202F771f50d3D14a367B48070;
    bytes32 constant salt = 0xf2d857f4a3edcb9b78b4d503bfe733db1e3f6cdc2b7971ee739626c97e86a558;

    string private constant EIP712_DOMAIN  = "EIP712Domain(string name,string version,uint256 chainId,address verifyingContract,bytes32 salt)";
    string private constant ARD_TYPE = "Ard(string id,address seller,address buyer,string duedate,uint256 total)";

    bytes32 private constant EIP712_DOMAIN_TYPEHASH = keccak256(abi.encodePacked(EIP712_DOMAIN));
    bytes32 private constant ARD_TYPEHASH = keccak256(abi.encodePacked(ARD_TYPE));
    bytes32 private constant DOMAIN_SEPARATOR = keccak256(abi.encode(
            EIP712_DOMAIN_TYPEHASH,
            keccak256("Account Receivable Signer"),
            keccak256("1"),
            chainId,
            verifyingContract,
            salt
        ));

    struct Ard {
        string id;
        address seller;
        address buyer;
        string duedate;
        uint256 total;
    }

    function hashArd(Ard memory ard) private pure returns (bytes32){
        return keccak256(abi.encodePacked(
                "\x19\x01",
                DOMAIN_SEPARATOR,
                keccak256(abi.encode(
                    ARD_TYPEHASH,
                    keccak256(ard.id),
                    ard.seller,
                    ard.buyer,
                    keccak256(ard.duedate),
                    ard.total
                ))
            ));
    }
    //"0xf2d857f4a3edcb9b78b4d503bfe733db1e3f6cdc2b7971ee739626c97e86a558","0x9c019724e411F546d3AC6D42861A5e7D81C0f497","0x9c019724e411F546d3AC6D42861A5e7D81C0f497","0xb06cc5315907b1af1b272dda5d76d7df662fd8804bb87e87a2a79f974b49909d",1000000

    function verify(string id, address seller, address buyer, string duedate, uint256 total, bytes32 r, bytes32 s, uint8 v) public pure returns (address) {
        Ard memory ard = Ard({
            id: id,
            seller: seller,
            buyer: buyer,
            duedate: duedate,
            total: total
            });

        return ecrecover(hashArd(ard), v, r, s);
    }
}
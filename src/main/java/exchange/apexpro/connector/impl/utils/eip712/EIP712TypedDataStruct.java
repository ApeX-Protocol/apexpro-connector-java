package exchange.apexpro.connector.impl.utils.eip712;

import exchange.apexpro.connector.impl.utils.BinaryUtil;
import lombok.extern.slf4j.Slf4j;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class EIP712TypedDataStruct {

    private EIP712TypedDataStruct() {
    }

    public static byte[] buildHashStruct(String primaryType, List<TypedData> typedData) {
        List<Type> typedHashData = new ArrayList<>();


        List<Type> schemaList = buildSchema(typedData);
        String sigTypes = primaryType + "("+schemaList.stream().map(t->t.getValue().toString()).collect(Collectors.joining(","))+")";
        typedHashData.add(new Bytes32(Hash.sha3(sigTypes.getBytes(StandardCharsets.UTF_8))));

        List<Type> hashedDataList = buildTypes(typedData);
        typedHashData.addAll(hashedDataList);

        log.trace("generateSignatureHash(typedData={}) ...", typedHashData);
        byte[] result = SolidityPackEncoder.soliditySHA3(typedHashData);

        return result;
    }

    //"EIP712Domain(string name,string version,uint256 chainId)"
    private static List<Type> buildSchema(List<TypedData> typedData) {
        List<Type> typeList = typedData.stream().map((t) -> new Utf8String(t.getType() + " " + t.getName()))
                .collect(Collectors.toList());

        return  typeList;
    }

    private static List<Type> buildTypes(List<TypedData> typedData) {

        return typedData.stream().map((t) -> {
            Type type = null;
            switch (t.getType().toLowerCase()) {
            case "string":
                byte[] bytes = Hash.sha3(t.getValue().toString().getBytes(StandardCharsets.UTF_8));
                String bytesHex = BinaryUtil.byteToHex(bytes);
                type = new Bytes32(bytes);
                break;
            case "uint":
                type = new Bytes32(Numeric.toBytesPadded(new BigInteger(t.getValue().toString()),32));
                break;
            case "uint256":
                type = new Bytes32(Numeric.toBytesPadded(new BigInteger(t.getValue().toString()),32));
                break;

            default:
                log.error("Unknow type [{}]", t.getType().toLowerCase());
                throw new IllegalArgumentException();
            }

            return type;
        }).collect(Collectors.toList());
    }

}

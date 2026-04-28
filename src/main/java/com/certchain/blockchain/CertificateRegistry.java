package com.certchain.blockchain;

import com.certchain.blockchain.CustomError;
import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/LFDT-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.8.0.
 */
@SuppressWarnings("rawtypes")
@Generated("org.web3j.codegen.SolidityFunctionWrapperGenerator")
public class CertificateRegistry extends Contract {
    public static final String BINARY = "Bin file was not provided";

    public static final String FUNC_CERTIFICATEEXISTS = "certificateExists";

    public static final String FUNC_GETCERTIFICATE = "getCertificate";

    public static final String FUNC_GETINSTITUTIONCERTIFICATES = "getInstitutionCertificates";

    public static final String FUNC_GETSTUDENTCERTIFICATES = "getStudentCertificates";

    public static final String FUNC_ISSUECERTIFICATE = "issueCertificate";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_PAUSE = "pause";

    public static final String FUNC_PAUSED = "paused";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_REVOKECERTIFICATE = "revokeCertificate";

    public static final String FUNC_TOTALCERTIFICATES = "totalCertificates";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final String FUNC_UNPAUSE = "unpause";

    public static final String FUNC_VERIFYCERTIFICATEBYHASH = "verifyCertificateByHash";

    public static final String FUNC_VERIFYCERTIFICATEBYID = "verifyCertificateById";

    public static final CustomError ENFORCEDPAUSE_ERROR = new CustomError("EnforcedPause", 
            Arrays.<TypeReference<?>>asList());
    ;

    public static final CustomError EXPECTEDPAUSE_ERROR = new CustomError("ExpectedPause", 
            Arrays.<TypeReference<?>>asList());
    ;

    public static final CustomError OWNABLEINVALIDOWNER_ERROR = new CustomError("OwnableInvalidOwner",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    ;

    public static final CustomError OWNABLEUNAUTHORIZEDACCOUNT_ERROR = new CustomError("OwnableUnauthorizedAccount", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    ;

    public static final Event CERTIFICATEISSUED_EVENT = new Event("CertificateIssued", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>(true) {}, new TypeReference<Bytes32>(true) {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event CERTIFICATEREVOKED_EVENT = new Event("CertificateRevoked", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event PAUSED_EVENT = new Event("Paused", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    ;

    public static final Event UNPAUSED_EVENT = new Event("Unpaused", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    ;

    @Deprecated
    protected CertificateRegistry(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected CertificateRegistry(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected CertificateRegistry(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected CertificateRegistry(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<CertificateIssuedEventResponse> getCertificateIssuedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(CERTIFICATEISSUED_EVENT, transactionReceipt);
        ArrayList<CertificateIssuedEventResponse> responses = new ArrayList<CertificateIssuedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            CertificateIssuedEventResponse typedResponse = new CertificateIssuedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.certId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.documentHash = (byte[]) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.institutionId = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.studentId = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.courseName = (String) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static CertificateIssuedEventResponse getCertificateIssuedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(CERTIFICATEISSUED_EVENT, log);
        CertificateIssuedEventResponse typedResponse = new CertificateIssuedEventResponse();
        typedResponse.log = log;
        typedResponse.certId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.documentHash = (byte[]) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.institutionId = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.studentId = (String) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.courseName = (String) eventValues.getNonIndexedValues().get(2).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
        return typedResponse;
    }

    public Flowable<CertificateIssuedEventResponse> certificateIssuedEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getCertificateIssuedEventFromLog(log));
    }

    public Flowable<CertificateIssuedEventResponse> certificateIssuedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CERTIFICATEISSUED_EVENT));
        return certificateIssuedEventFlowable(filter);
    }

    public static List<CertificateRevokedEventResponse> getCertificateRevokedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(CERTIFICATEREVOKED_EVENT, transactionReceipt);
        ArrayList<CertificateRevokedEventResponse> responses = new ArrayList<CertificateRevokedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            CertificateRevokedEventResponse typedResponse = new CertificateRevokedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.certId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.revokedBy = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static CertificateRevokedEventResponse getCertificateRevokedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(CERTIFICATEREVOKED_EVENT, log);
        CertificateRevokedEventResponse typedResponse = new CertificateRevokedEventResponse();
        typedResponse.log = log;
        typedResponse.certId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.revokedBy = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<CertificateRevokedEventResponse> certificateRevokedEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getCertificateRevokedEventFromLog(log));
    }

    public Flowable<CertificateRevokedEventResponse> certificateRevokedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CERTIFICATEREVOKED_EVENT));
        return certificateRevokedEventFlowable(filter);
    }

    public static List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static OwnershipTransferredEventResponse getOwnershipTransferredEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
        OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
        typedResponse.log = log;
        typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOwnershipTransferredEventFromLog(log));
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventFlowable(filter);
    }

    public static List<PausedEventResponse> getPausedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(PAUSED_EVENT, transactionReceipt);
        ArrayList<PausedEventResponse> responses = new ArrayList<PausedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PausedEventResponse typedResponse = new PausedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.account = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static PausedEventResponse getPausedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(PAUSED_EVENT, log);
        PausedEventResponse typedResponse = new PausedEventResponse();
        typedResponse.log = log;
        typedResponse.account = (String) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<PausedEventResponse> pausedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getPausedEventFromLog(log));
    }

    public Flowable<PausedEventResponse> pausedEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PAUSED_EVENT));
        return pausedEventFlowable(filter);
    }

    public static List<UnpausedEventResponse> getUnpausedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(UNPAUSED_EVENT, transactionReceipt);
        ArrayList<UnpausedEventResponse> responses = new ArrayList<UnpausedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            UnpausedEventResponse typedResponse = new UnpausedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.account = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static UnpausedEventResponse getUnpausedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(UNPAUSED_EVENT, log);
        UnpausedEventResponse typedResponse = new UnpausedEventResponse();
        typedResponse.log = log;
        typedResponse.account = (String) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<UnpausedEventResponse> unpausedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getUnpausedEventFromLog(log));
    }

    public Flowable<UnpausedEventResponse> unpausedEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(UNPAUSED_EVENT));
        return unpausedEventFlowable(filter);
    }

    public RemoteFunctionCall<Boolean> certificateExists(String certId) {
        final Function function = new Function(FUNC_CERTIFICATEEXISTS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(certId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<Certificate> getCertificate(String certId) {
        final Function function = new Function(FUNC_GETCERTIFICATE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(certId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Certificate>() {}));
        return executeRemoteCallSingleValueReturn(function, Certificate.class);
    }

    public RemoteFunctionCall<List> getInstitutionCertificates(String institutionId) {
        final Function function = new Function(FUNC_GETINSTITUTIONCERTIFICATES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(institutionId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Utf8String>>() {}));
        return new RemoteFunctionCall<List>(function,
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteFunctionCall<List> getStudentCertificates(String studentId) {
        final Function function = new Function(FUNC_GETSTUDENTCERTIFICATES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(studentId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Utf8String>>() {}));
        return new RemoteFunctionCall<List>(function,
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> issueCertificate(String certId,
            byte[] documentHash, String ipfsCID, String institutionId, String studentId,
            String studentName, String courseName) {
        final Function function = new Function(
                FUNC_ISSUECERTIFICATE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(certId), 
                new org.web3j.abi.datatypes.generated.Bytes32(documentHash), 
                new org.web3j.abi.datatypes.Utf8String(ipfsCID), 
                new org.web3j.abi.datatypes.Utf8String(institutionId), 
                new org.web3j.abi.datatypes.Utf8String(studentId), 
                new org.web3j.abi.datatypes.Utf8String(studentName), 
                new org.web3j.abi.datatypes.Utf8String(courseName)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> pause() {
        final Function function = new Function(
                FUNC_PAUSE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> paused() {
        final Function function = new Function(FUNC_PAUSED, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(
                FUNC_RENOUNCEOWNERSHIP, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> revokeCertificate(String certId) {
        final Function function = new Function(
                FUNC_REVOKECERTIFICATE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(certId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> totalCertificates() {
        final Function function = new Function(FUNC_TOTALCERTIFICATES, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final Function function = new Function(
                FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> unpause() {
        final Function function = new Function(
                FUNC_UNPAUSE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple2<Boolean, String>> verifyCertificateByHash(
            byte[] documentHash) {
        final Function function = new Function(FUNC_VERIFYCERTIFICATEBYHASH, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(documentHash)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}, new TypeReference<Utf8String>() {}));
        return new RemoteFunctionCall<Tuple2<Boolean, String>>(function,
                new Callable<Tuple2<Boolean, String>>() {
                    @Override
                    public Tuple2<Boolean, String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<Boolean, String>(
                                (Boolean) results.get(0).getValue(), 
                                (String) results.get(1).getValue());
                    }
                });
    }

    public RemoteFunctionCall<VerifyResult> verifyCertificateById(String certId) {
        final Function function = new Function(FUNC_VERIFYCERTIFICATEBYID, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(certId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<VerifyResult>() {}));
        return executeRemoteCallSingleValueReturn(function, VerifyResult.class);
    }

    @Deprecated
    public static CertificateRegistry load(String contractAddress, Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new CertificateRegistry(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static CertificateRegistry load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new CertificateRegistry(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static CertificateRegistry load(String contractAddress, Web3j web3j,
            Credentials credentials, ContractGasProvider contractGasProvider) {
        return new CertificateRegistry(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static CertificateRegistry load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new CertificateRegistry(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static class Certificate extends DynamicStruct {
        public String certId;

        public byte[] documentHash;

        public String ipfsCID;

        public String institutionId;

        public String studentId;

        public String studentName;

        public String courseName;

        public BigInteger issuedAt;

        public BigInteger status;

        public Certificate(String certId, byte[] documentHash, String ipfsCID, String institutionId,
                String studentId, String studentName, String courseName, BigInteger issuedAt,
                BigInteger status) {
            super(new org.web3j.abi.datatypes.Utf8String(certId), 
                    new org.web3j.abi.datatypes.generated.Bytes32(documentHash), 
                    new org.web3j.abi.datatypes.Utf8String(ipfsCID), 
                    new org.web3j.abi.datatypes.Utf8String(institutionId), 
                    new org.web3j.abi.datatypes.Utf8String(studentId), 
                    new org.web3j.abi.datatypes.Utf8String(studentName), 
                    new org.web3j.abi.datatypes.Utf8String(courseName), 
                    new org.web3j.abi.datatypes.generated.Uint256(issuedAt), 
                    new org.web3j.abi.datatypes.generated.Uint8(status));
            this.certId = certId;
            this.documentHash = documentHash;
            this.ipfsCID = ipfsCID;
            this.institutionId = institutionId;
            this.studentId = studentId;
            this.studentName = studentName;
            this.courseName = courseName;
            this.issuedAt = issuedAt;
            this.status = status;
        }

        public Certificate(Utf8String certId, Bytes32 documentHash, Utf8String ipfsCID,
                Utf8String institutionId, Utf8String studentId, Utf8String studentName,
                Utf8String courseName, Uint256 issuedAt, Uint8 status) {
            super(certId, documentHash, ipfsCID, institutionId, studentId, studentName, courseName, issuedAt, status);
            this.certId = certId.getValue();
            this.documentHash = documentHash.getValue();
            this.ipfsCID = ipfsCID.getValue();
            this.institutionId = institutionId.getValue();
            this.studentId = studentId.getValue();
            this.studentName = studentName.getValue();
            this.courseName = courseName.getValue();
            this.issuedAt = issuedAt.getValue();
            this.status = status.getValue();
        }
    }

    public static class VerifyResult extends DynamicStruct {
        public Boolean isValid;

        public BigInteger status;

        public String institution;

        public String student;

        public String course;

        public BigInteger issuedAt;

        public String ipfsCID;

        public VerifyResult(Boolean isValid, BigInteger status, String institution, String student,
                String course, BigInteger issuedAt, String ipfsCID) {
            super(new org.web3j.abi.datatypes.Bool(isValid), 
                    new org.web3j.abi.datatypes.generated.Uint8(status), 
                    new org.web3j.abi.datatypes.Utf8String(institution), 
                    new org.web3j.abi.datatypes.Utf8String(student), 
                    new org.web3j.abi.datatypes.Utf8String(course), 
                    new org.web3j.abi.datatypes.generated.Uint256(issuedAt), 
                    new org.web3j.abi.datatypes.Utf8String(ipfsCID));
            this.isValid = isValid;
            this.status = status;
            this.institution = institution;
            this.student = student;
            this.course = course;
            this.issuedAt = issuedAt;
            this.ipfsCID = ipfsCID;
        }

        public VerifyResult(Bool isValid, Uint8 status, Utf8String institution, Utf8String student,
                Utf8String course, Uint256 issuedAt, Utf8String ipfsCID) {
            super(isValid, status, institution, student, course, issuedAt, ipfsCID);
            this.isValid = isValid.getValue();
            this.status = status.getValue();
            this.institution = institution.getValue();
            this.student = student.getValue();
            this.course = course.getValue();
            this.issuedAt = issuedAt.getValue();
            this.ipfsCID = ipfsCID.getValue();
        }
    }

    public static class CertificateIssuedEventResponse extends BaseEventResponse {
        public byte[] certId;

        public byte[] documentHash;

        public String institutionId;

        public String studentId;

        public String courseName;

        public BigInteger timestamp;
    }

    public static class CertificateRevokedEventResponse extends BaseEventResponse {
        public byte[] certId;

        public String revokedBy;

        public BigInteger timestamp;
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public String previousOwner;

        public String newOwner;
    }

    public static class PausedEventResponse extends BaseEventResponse {
        public String account;
    }

    public static class UnpausedEventResponse extends BaseEventResponse {
        public String account;
    }
}

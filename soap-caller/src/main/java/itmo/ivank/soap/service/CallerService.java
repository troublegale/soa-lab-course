package itmo.ivank.soap.service;

import itmo.ivank.soap.dto.Acquiring;
import itmo.ivank.soap.dto.FireResponse;
import itmo.ivank.soap.exception.CallerServiceException;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService(
    name = "CallerService",
    targetNamespace = "http://soap.ivank.itmo/caller"
)
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
public interface CallerService {

    @WebMethod(operationName = "acquire")
    @WebResult(name = "acquiring")
    Acquiring acquire(
        @WebParam(name = "acquirerId") Long acquirerId,
        @WebParam(name = "acquiredId") Long acquiredId
    ) throws CallerServiceException;

    @WebMethod(operationName = "fireAllOrgEmployees")
    @WebResult(name = "fireResponse")
    FireResponse fireAllOrgEmployees(
        @WebParam(name = "organizationId") Long organizationId
    ) throws CallerServiceException;
}

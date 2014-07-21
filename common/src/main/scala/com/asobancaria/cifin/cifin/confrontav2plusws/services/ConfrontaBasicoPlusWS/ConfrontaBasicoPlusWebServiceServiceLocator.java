/**
 * ConfrontaBasicoPlusWebServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.asobancaria.cifin.cifin.confrontav2plusws.services.ConfrontaBasicoPlusWS;

public class ConfrontaBasicoPlusWebServiceServiceLocator extends org.apache.axis.client.Service implements ConfrontaBasicoPlusWebServiceService {

    public ConfrontaBasicoPlusWebServiceServiceLocator() {
    }


    public ConfrontaBasicoPlusWebServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ConfrontaBasicoPlusWebServiceServiceLocator(String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for ConfrontaBasicoPlusWS
    private String ConfrontaBasicoPlusWS_address = "http://cifin.asobancaria.com/cifin/confrontav2plusws/services/ConfrontaBasicoPlusWS";

    public String getConfrontaBasicoPlusWSAddress() {
        return ConfrontaBasicoPlusWS_address;
    }

    // The WSDD service name defaults to the port name.
    private String ConfrontaBasicoPlusWSWSDDServiceName = "ConfrontaBasicoPlusWS";

    public String getConfrontaBasicoPlusWSWSDDServiceName() {
        return ConfrontaBasicoPlusWSWSDDServiceName;
    }

    public void setConfrontaBasicoPlusWSWSDDServiceName(String name) {
        ConfrontaBasicoPlusWSWSDDServiceName = name;
    }

    public ConfrontaBasicoPlusWebService getConfrontaBasicoPlusWS() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ConfrontaBasicoPlusWS_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getConfrontaBasicoPlusWS(endpoint);
    }

    public ConfrontaBasicoPlusWebService getConfrontaBasicoPlusWS(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            ConfrontaBasicoPlusWSSoapBindingStub _stub = new ConfrontaBasicoPlusWSSoapBindingStub(portAddress, this);
            _stub.setPortName(getConfrontaBasicoPlusWSWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setConfrontaBasicoPlusWSEndpointAddress(String address) {
        ConfrontaBasicoPlusWS_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (ConfrontaBasicoPlusWebService.class.isAssignableFrom(serviceEndpointInterface)) {
                ConfrontaBasicoPlusWSSoapBindingStub _stub = new ConfrontaBasicoPlusWSSoapBindingStub(new java.net.URL(ConfrontaBasicoPlusWS_address), this);
                _stub.setPortName(getConfrontaBasicoPlusWSWSDDServiceName());
                return _stub;
            }
        }
        catch (Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        String inputPortName = portName.getLocalPart();
        if ("ConfrontaBasicoPlusWS".equals(inputPortName)) {
            return getConfrontaBasicoPlusWS();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://cifin.asobancaria.com/cifin/confrontav2plusws/services/ConfrontaBasicoPlusWS", "ConfrontaBasicoPlusWebServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://cifin.asobancaria.com/cifin/confrontav2plusws/services/ConfrontaBasicoPlusWS", "ConfrontaBasicoPlusWS"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(String portName, String address) throws javax.xml.rpc.ServiceException {
        
if ("ConfrontaBasicoPlusWS".equals(portName)) {
            setConfrontaBasicoPlusWSEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}

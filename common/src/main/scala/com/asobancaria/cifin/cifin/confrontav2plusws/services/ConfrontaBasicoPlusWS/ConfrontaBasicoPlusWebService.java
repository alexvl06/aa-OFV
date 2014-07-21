/**
 * ConfrontaBasicoPlusWebService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.asobancaria.cifin.cifin.confrontav2plusws.services.ConfrontaBasicoPlusWS;

public interface ConfrontaBasicoPlusWebService extends java.rmi.Remote {
    public com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.CuestionarioBPDTO obtenerCuestionarioAdicional(com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ParametrosSeguridadBPDTO p_parametrosSeguridad, com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ParametrosAdicionalBPDTO p_parametros) throws java.rmi.RemoteException;
    public com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.CuestionarioBPDTO obtenerCuestionario(com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ParametrosSeguridadBPDTO p_parametrosSeguridad, com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ParametrosBPDTO p_parametros) throws java.rmi.RemoteException;
    public com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ResultadoEvaluacionCuestionarioBPDTO evaluarCuestionario(com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.ParametrosSeguridadBPDTO p_parametrosSeguridad, com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.RespuestaCuestionarioBPDTO p_respuestaCuestionario) throws java.rmi.RemoteException;
}

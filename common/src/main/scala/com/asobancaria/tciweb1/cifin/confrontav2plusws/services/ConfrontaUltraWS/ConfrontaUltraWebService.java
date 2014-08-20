/**
 * ConfrontaUltraWebService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.asobancaria.tciweb1.cifin.confrontav2plusws.services.ConfrontaUltraWS;

public interface ConfrontaUltraWebService extends java.rmi.Remote {
    public co.cifin.confrontaultra.dto.ultra.CuestionarioULTRADTO obtenerCuestionario(co.cifin.confrontaultra.dto.ultra.ParametrosSeguridadULTRADTO p_parametrosSeguridad, co.cifin.confrontaultra.dto.ultra.ParametrosULTRADTO p_parametros) throws java.rmi.RemoteException;
    public co.cifin.confrontaultra.dto.ultra.CuestionarioULTRADTO obtenerCuestionarioAdicional(co.cifin.confrontaultra.dto.ultra.ParametrosSeguridadULTRADTO p_parametrosSeguridad, co.cifin.confrontaultra.dto.ultra.ParametrosAdicionalULTRADTO p_parametros) throws java.rmi.RemoteException;
    public co.cifin.confrontaultra.dto.ultra.ResultadoEvaluacionCuestionarioULTRADTO evaluarCuestionario(co.cifin.confrontaultra.dto.ultra.ParametrosSeguridadULTRADTO p_parametrosSeguridad, co.cifin.confrontaultra.dto.ultras.RespuestaCuestionarioULTRADTO p_respuestaCuestionario) throws java.rmi.RemoteException;
}

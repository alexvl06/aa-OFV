package co.com.alianza.infrastructure.dto

/**
 *
 * @author smontanez
 */
case class Cliente(wcli_nombre: String, wcli_person: String, wcli_estado: String, wcli_estado_descri: String, wcli_dir_correo: String,
  wcli_ident_replegal: String, wcli_cias_pagos_masivos: String, wcli_constructor: String)

case class MiembroGrupo(nombre: String, correo: String, estado: String, tipo: String) {
  def toCliente = Cliente(nombre, tipo, estado, "", correo, "", "", "")
}


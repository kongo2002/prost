package com.kongo2002.android.prost

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast

import ImplicitHelpers._


object UI {

  /**
   * Create and show a Toast for a specified period of time.
   */
  def toast(duration: Int)(ctx: Context, msg: String) {
    Toast.makeText(ctx, msg, duration).show()
  }

  /**
   * Create and show a Toast for a long period of time.
   */
  def longToast(ctx: Context, msg: String) = toast(Toast.LENGTH_LONG) _

  /**
   * Create and show a Toast for a short period of time.
   */
  def shortToast(ctx: Context, msg: String) = toast(Toast.LENGTH_SHORT) _

  /**
   * Create and show a confirmation dialog and hook into
   * the specified callback function.
   */
  def confirm(ctx: Context, title: String, question: String, ok: (DialogInterface, Int) => Unit) {
    val builder = new AlertDialog.Builder(ctx)

    /* set texts */
    builder.setTitle(title)
    builder.setMessage(question)

    /* add buttons and their callbacks */
    builder.setPositiveButton(R.string.ok, ok)
    builder.setNegativeButton(R.string.cancel, (di: DialogInterface, i: Int) => {})

    /* create and show dialog */
    val dialog = builder.create
    dialog.show
  }

  /**
   * Create and show a dialog with a list selection and hook
   * into the specified callback function.
   */
  def listSelect(ctx: Context, title: Int, items: Int, choice: Int, ok: (DialogInterface, Int) => Unit) {
    val builder = new AlertDialog.Builder(ctx)

    /* set title and items to select from */
    builder.setTitle(title)
    builder.setSingleChoiceItems(items, choice, ok)

    /* create and show dialog */
    val dialog = builder.create
    dialog.show
  }

}
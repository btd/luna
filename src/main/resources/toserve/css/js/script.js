/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

/* Author:
 Bardadym Denis
 */
$(function() {
    $('form > input.textfield')
        .focus(function() {
            $(this).removeClass('not_active');
            if (!this._defaultValue) {
                this._defaultValue = this.value;
            }
            if (!this._hasChanged) {
                this.value = '';
                this._hasChanged = true;
            }
        })
        .blur(function() {
            if (!this.value) {
                this.value = this._defaultValue;
                this._hasChanged = false;
                $(this).addClass('not_active');
            }
        });
    $('form > textarea')
        .focus(function() {
            $(this).removeClass('not_active');
            if (!this._defaultValue) {
                this._defaultValue = $(this).val();
            }
            if (!this._hasChanged) {
                $(this).val('');
                this._hasChanged = true;
            }
        })
        .blur(function() {
            if (!this.value) {
                $(this).val(this._defaultValue);
                this._hasChanged = false;
                $(this).addClass('not_active');
            }
        });

    //$('.address_list').
})












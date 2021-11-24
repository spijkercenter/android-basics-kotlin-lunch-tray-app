/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.lunchtray.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.lunchtray.data.DataSource
import java.text.NumberFormat
import java.util.*
import kotlin.reflect.KMutableProperty0

class OrderViewModel : ViewModel() {

    // Map of menu items
    val menuItems = DataSource.menuItems

    // Default values for item prices
    private var previousEntreePrice = 0.0
    private var previousSidePrice = 0.0
    private var previousAccompanimentPrice = 0.0

    // Default tax rate
    private val taxRate = 0.08

    // Entree for the order
    private val _entree = MutableLiveData<MenuItem?>()
    val entree: LiveData<MenuItem?> = _entree

    // Side for the order
    private val _side = MutableLiveData<MenuItem?>()
    val side: LiveData<MenuItem?> = _side

    // Accompaniment for the order.
    private val _accompaniment = MutableLiveData<MenuItem?>()
    val accompaniment: LiveData<MenuItem?> = _accompaniment

    // Subtotal for the order
    private val _subtotal = MutableLiveData(0.0)
    val subtotal: LiveData<String> = Transformations.map(_subtotal) {
        NumberFormat.getCurrencyInstance(Locale.US).format(it)
    }

    // Total cost of the order
    private val _total = MutableLiveData(0.0)
    val total: LiveData<String> = Transformations.map(_total) {
        NumberFormat.getCurrencyInstance(Locale.US).format(it)
    }

    // Tax for the order
    private val _tax = MutableLiveData(0.0)
    val tax: LiveData<String> = Transformations.map(_tax) {
        NumberFormat.getCurrencyInstance(Locale.US).format(it)
    }

    /**
     * Set the entree for the order.
     */
    fun setEntree(entree: String) =
        this.set(entree, _entree, this::previousEntreePrice)


    /**
     * Set the side for the order.
     */
    fun setSide(side: String) =
        this.set(side, _side, this::previousSidePrice)

    /**
     * Set the accompaniment for the order.
     */
    fun setAccompaniment(accompaniment: String) =
        this.set(accompaniment, _accompaniment, this::previousAccompanimentPrice)

    private fun set(
        meal: String,
        prop: MutableLiveData<MenuItem?>,
        prev: KMutableProperty0<Double>
    ) {
        val item = menuItems[meal]!!

        prop.value?.let { prev.set(it.price) }

        _subtotal.value = _subtotal.value?.let { it - prev.get() }

        prop.value = item
        updateSubtotal(item.price)
    }

    /**
     * Update subtotal value.
     */
    private fun updateSubtotal(itemPrice: Double) {
        // if _subtotal.value is not null, update it to reflect the price of the recently
        //  added item.
        //  Otherwise, set _subtotal.value to equal the price of the item.
        _subtotal.value = (_subtotal.value ?: 0.0) + itemPrice

        // calculate the tax and resulting total
        calculateTaxAndTotal()
    }

    /**
     * Calculate tax and update total.
     */
    fun calculateTaxAndTotal() {
        val subTotal = _subtotal.value ?: 0.0
        val tax = subTotal * taxRate
        // set _tax.value based on the subtotal and the tax rate.
        _tax.value = tax
        // set the total based on the subtotal and _tax.value.
        _total.value = subTotal + tax
    }

    /**
     * Reset all values pertaining to the order.
     */
    fun resetOrder() {
        // Reset all values associated with an order
        _entree.value = null
        _side.value = null
        _accompaniment.value = null
        _subtotal.value = 0.0
        _tax.value = 0.0
        _total.value = 0.0
    }
}
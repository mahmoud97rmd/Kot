package com.tradingapp.metatrader.app.features.drawing.store

import com.tradingapp.metatrader.app.features.drawing.model.DrawingObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DrawingStore @Inject constructor() {

    private val _items = MutableStateFlow<List<DrawingObject>>(emptyList())
    val items: StateFlow<List<DrawingObject>> = _items.asStateFlow()

    @Synchronized
    fun setAll(list: List<DrawingObject>) { _items.value = list }

    @Synchronized
    fun add(obj: DrawingObject) { _items.value = _items.value + obj }

    @Synchronized
    fun removeById(id: String) { _items.value = _items.value.filterNot { it.id == id } }

    @Synchronized
    fun update(obj: DrawingObject) {
        _items.value = _items.value.map { if (it.id == obj.id) obj else it }
    }

    @Synchronized
    fun clear() { _items.value = emptyList() }
}

package com.sg8.storage.adapter.flatfile

// From the Cobblemon Mod I adapted this script from
/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can get one at https://mozilla.org/MPL/2.0/.
 *
 * @author Hiroku
 * @since November 29th, 2021
 *
 * package com.cobblemon.mod.common.api.storage.adapter.flatfile *
 * open class JSONStoreAdapter(
 *     rootFolder: String,
 *     useNestedFolders: Boolean,
 *     folderPerClass: Boolean,
 *     private val gson: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(),
 * ) : OneToOneFileStoreAdapter<JsonObject>(rootFolder, useNestedFolders, folderPerClass, "json")
 */

import com.cobblemon.mod.common.util.fromJson
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.sg8.storage.Store
import com.sg8.storage.StorePosition
import com.sg8.storage.TypeStored
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.PrintWriter
import java.util.UUID

/**
 * A [FileStoreAdapter] for JSON files. This allows a [Store] to be serialized to a .json file. This is usually
 * slower and makes for a larger file per storage by several times compared to a [NbtStoreAdapter].
 */
// Eight's implementation
abstract class JSONStoreAdapter(
    rootFolder: String,
    useNestedFolders: Boolean,
    folderPerClass: Boolean,
    private val gson: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(),
): OneToOneFileStoreAdapter<JsonObject>(
    rootFolder = rootFolder,
    useNestedFolders = useNestedFolders,
    folderPerClass = folderPerClass,
    fileExtension = "json",
) {

    override fun <P : StorePosition, T : TypeStored, S : Store<P, T>> serialize(
        store: S,
    ): JsonObject {
        return store.saveToJson(json = JsonObject())
    }

    override fun save(file: File, serialized: JsonObject) {
        val pw = PrintWriter(file)
        val json = gson.toJson(serialized)
        pw.write(json)
        pw.flush()
        pw.close()
    }

    override fun <P : StorePosition, T : TypeStored, S : Store<P, T>> load(
        file: File,
        storeClass: Class<out S>,
        uuid: UUID,
    ): S? {
        return try {
            val br = BufferedReader(FileReader(file))
            val json = gson.fromJson<JsonObject>(br)
            br.close()

            val store = try {
                storeClass.getConstructor(UUID::class.java, UUID::class.java).newInstance(uuid, uuid)
            } catch (exception: NoSuchMethodException) {
                storeClass.getConstructor(UUID::class.java).newInstance(uuid)
            }

            store.loadFromJson(json)
            store
        } catch (e: Exception) {
            null
        }
    }

}

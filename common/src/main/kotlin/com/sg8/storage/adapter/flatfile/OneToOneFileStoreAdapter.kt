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
 * @since November 30th, 2021
 *
 * package com.cobblemon.mod.common.api.storage.adapter.flatfile
 *
 * abstract class OneToOneFileStoreAdapter<Ser>(
 * private val rootFolder: String,
 * private val useNestedFolders: Boolean,
 * private val folderPerClass: Boolean,
 * private val fileExtension: String,
 * ) : FileStoreAdapter<S>, CobblemonAdapterParent<S>() {
 *
 */

import com.cobblemontournament.common.CobblemonTournament.LOGGER
import com.sg8.storage.Store
import com.sg8.storage.StorePosition
import com.sg8.storage.TypeStored
import com.sg8.storage.adapter.StoreAdapterParent
import java.io.File
import java.util.UUID

// Eight's implementation
abstract class OneToOneFileStoreAdapter<Ser>(
    private val rootFolder: String,
    private val useNestedFolders: Boolean,
    private val folderPerClass: Boolean,
    private val fileExtension: String,
) : FileStoreAdapter<Ser>, StoreAdapterParent<Ser>() {

    abstract fun save(file: File, serialized: Ser)

    abstract fun <P : StorePosition, T : TypeStored, S : Store<P, T>> load(
        file: File,
        storeClass: Class<out S>,
        uuid: UUID,
    ): S?

    override fun save(
        storeClass: Class<out Store<*, *>>,
        uuid: UUID,
        serialized: Ser,
    ) {
        val file = getFile(storeClass, uuid)
        val tempFile = File((file.absolutePath + ".temp"))
        tempFile.createNewFile()
        save(tempFile, serialized)
        tempFile.copyTo(file, overwrite = true)
        tempFile.delete()
    }

    override fun <P: StorePosition,T: TypeStored,S: Store<P, T>> provide(
        storeClass: Class<S>,
        uuid: UUID
    ): S? {
        val file = getFile(storeClass, uuid)
        val tempFile = File((file.absolutePath + ".temp"))
        if (tempFile.exists()) {
            try {
                val tempLoaded = load(tempFile, storeClass, uuid)
                if (tempLoaded != null) {
                    save(file = file, serialized = serialize(store = tempLoaded))
                    return tempLoaded
                }
            } finally {
                tempFile.delete()
            }
        }

        return if (file.exists()) {
            load(file, storeClass, uuid)
                ?: let { _ ->
                    LOGGER.error("TypeToStore save file for ${storeClass.simpleName} ($uuid) was corrupted. A fresh file will be created.")
                    storeClass.getConstructor(UUID::class.java).newInstance(uuid)
                }
        } else {
            null
        }
    }

    private fun getFile(
        storeClass: Class<out Store<*, *>>,
        uuid: UUID,
    ): File {
        val className = storeClass.simpleName.lowercase()
        val subfolder1 = if (folderPerClass) ("$className/") else ""
        val subfolder2 = if (useNestedFolders) ("${uuid.toString().substring(0, 2)}/") else ""
        val folder = if (!rootFolder.endsWith(("/"))) ("$rootFolder/") else rootFolder
        val fileName = if (folderPerClass) ("$uuid.$fileExtension") else ("$uuid-$className.$fileExtension")
        val file = File((folder + subfolder1 + subfolder2), fileName)
        file.parentFile.mkdirs()
        return file
    }

}

package com.someguy.storage.adapter.flatfile

// From the Cobblemon Mod I adapted this script from
/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * @author Hiroku
 * @since November 30th, 2021
 *
 * package com.cobblemon.mod.common.api.storage.adapter.flatfile
 * open class NBTStoreAdapter(
 * rootFolder: String,
 * useNestedFolders: Boolean,
 * folderPerClass: Boolean,
 * ) : OneToOneFileStoreAdapter<NbtCompound>(rootFolder, useNestedFolders, folderPerClass, "dat")
 */

import com.someguy.storage.store.Store
import com.someguy.storage.position.StorePosition
import com.someguy.storage.classstored.ClassStored
import java.io.File
import java.util.UUID
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo

/**
 * A [OneToOneFileStoreAdapter] that can arbitrarily save a single [Store] into an [CompoundTag] with the
 * help of Minecraft's [NbtIo]. This is arguably the best persistence method for [Store]s and is absolutely
 * the most efficient [FileStoreAdapter].
 */
// Eight's implementation
open class NBTStoreAdapter(
    rootFolder: String,
    useNestedFolders: Boolean,
    folderPerClass: Boolean,
) : OneToOneFileStoreAdapter<CompoundTag>(rootFolder, useNestedFolders, folderPerClass, "dat") {

    override fun <P: StorePosition,T: ClassStored,St: Store<P, T>> serialize(store: St) = store.saveToNBT(CompoundTag())

    override fun save(file: File,serialized: CompoundTag) = NbtIo.writeCompressed(serialized, file)

    override fun <P : StorePosition, T : ClassStored, St : Store<P, T>> load(
        file: File,
        storeClass: Class<out St>,
        uuid: UUID,
    ): St? {
        val store = try {
            storeClass.getConstructor(UUID::class.java).newInstance(uuid)
        } catch (exception: NoSuchMethodException) {
            storeClass.getConstructor(UUID::class.java).newInstance(uuid)
        }
        return try {
            val nbt = NbtIo.readCompressed(file)
            store.loadFromNBT(nbt)
            store
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}

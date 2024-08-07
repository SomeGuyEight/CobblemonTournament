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
 * package com.cobblemon.mod.common.api.storage.adapter.flatfile
 * interface FileStoreAdapter<S>: CobblemonAdapter<S>
 */

import com.sg8.storage.Store
import com.sg8.storage.StorePosition
import com.sg8.storage.TypeStored
import com.sg8.storage.adapter.StoreAdapter
import java.util.UUID

// Eight's implementation
interface FileStoreAdapter<Ser> : StoreAdapter<Ser> {

    /**
     * Converts the specified store into a serialized form.
     * This is expected to run on the server thread, and as fast as possible.
     */
    fun <P: StorePosition,T: TypeStored,S: Store<P, T>> serialize(store: S): Ser

    /**
     * Writes the serialized form of a store into the appropriate file.
     * This should be thread safe.
     */
    fun save(storeClass: Class<out Store<*, *>>, uuid: UUID, serialized: Ser)

}

package com.lumina.luminahotels.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://ugcyptfjwsrdcjtymasr.supabase.co",
        supabaseKey = "sb_publishable_EZCAqI7-Z04wQbLpHrgZ6w_LnvMdUvm"
    ) {
        install(Postgrest)
        install(Storage)
        install(Realtime)
    }
}

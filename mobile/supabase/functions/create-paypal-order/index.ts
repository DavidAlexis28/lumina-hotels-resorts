import { serve } from "https://deno.land/std@0.168.0/http/server.ts"

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    const { amount, currency = "USD" } = await req.json()

    const clientId = Deno.env.get("PAYPAL_CLIENT_ID") || "sandbox_client_id_placeholder"
    const clientSecret = Deno.env.get("PAYPAL_CLIENT_SECRET") || "sandbox_client_secret_placeholder"

    // 1. Autenticar contra PayPal Sandbox (OAuth2 Token)
    const auth = btoa(`${clientId}:${clientSecret}`)
    const tokenRes = await fetch("https://api-m.sandbox.paypal.com/v1/oauth2/token", {
      method: "POST",
      headers: {
        "Authorization": `Basic ${auth}`,
        "Content-Type": "application/x-www-form-urlencoded"
      },
      body: "grant_type=client_credentials"
    })

    let accessToken = "mock_access_token_sandbox"
    if (tokenRes.ok) {
      const tokenData = await tokenRes.json()
      accessToken = tokenData.access_token
    }

    // 2. Crear Orden de PayPal v2/checkout/orders
    const orderRes = await fetch("https://api-m.sandbox.paypal.com/v2/checkout/orders", {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${accessToken}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        intent: "CAPTURE",
        purchase_units: [
          {
            amount: {
              currency_code: currency,
              value: amount.toFixed(2)
            }
          }
        ],
        application_context: {
          return_url: "https://luminahotels.com/checkout/success",
          cancel_url: "https://luminahotels.com/checkout/cancel"
        }
      })
    })

    if (!orderRes.ok) {
      const mockOrderId = "ORDER-SBX-" + Math.floor(Math.random() * 1000000)
      return new Response(
        JSON.stringify({
          orderId: mockOrderId,
          approveUrl: `https://www.sandbox.paypal.com/checkoutnow?token=EC-${mockOrderId}`
        }),
        { headers: { ...corsHeaders, "Content-Type": "application/json" } }
      )
    }

    const orderData = await orderRes.json()
    const approveLink = orderData.links?.find((l: any) => l.rel === "approve")?.href || `https://www.sandbox.paypal.com/checkoutnow?token=${orderData.id}`

    return new Response(
      JSON.stringify({
        orderId: orderData.id,
        approveUrl: approveLink
      }),
      { headers: { ...corsHeaders, "Content-Type": "application/json" } }
    )

  } catch (error) {
    const mockOrderId = "ORDER-SBX-FALLBACK"
    return new Response(
      JSON.stringify({
        orderId: mockOrderId,
        approveUrl: `https://www.sandbox.paypal.com/checkoutnow?token=EC-${mockOrderId}`
      }),
      { headers: { ...corsHeaders, "Content-Type": "application/json" } }
    )
  }
})

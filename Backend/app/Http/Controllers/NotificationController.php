<?php

namespace App\Http\Controllers;

use App\Models\Notification;
use Illuminate\Http\Request;


class NotificationController extends Controller
{
    public function store(Request $request)
    {
        $validated = $request->validate([
            'app_name' => 'required|string',
            'app_identifier' => 'nullable|string',
            'app_version' => 'nullable|string',

            // Sender info
            'sender_id' => 'nullable|string',
            'sender_name' => 'nullable|string',
            'sender_username' => 'nullable|string',
            'sender_profile_url' => 'nullable|string',
            'sender_verified' => 'nullable|boolean',
            'sender_phone' => 'nullable|string',
            'sender_email' => 'nullable|email',

            // Receiver info
            'receiver_id' => 'nullable|string',
            'receiver_name' => 'nullable|string',
            'receiver_username' => 'nullable|string',
            'receiver_phone' => 'nullable|string',
            'receiver_email' => 'nullable|email',

            // Content
            'title' => 'nullable|string',
            'message' => 'nullable|string',
            'message_type' => 'nullable|in:text,image,video,audio,file,link,call,sticker,system',

            // Media
            'media_url' => 'nullable|string',
            'media_type' => 'nullable|string',
            'media_size' => 'nullable|integer',
            'media_duration' => 'nullable|integer',
            'media_metadata' => 'nullable|array',

            // Call info
            'call_type' => 'nullable|in:incoming,outgoing,missed',
            'call_duration' => 'nullable|integer',

            // Status
            'is_read' => 'nullable|boolean',
            'is_deleted' => 'nullable|boolean',
            'delivered_at' => 'nullable|date',
            'read_at' => 'nullable|date',

            // Device & Network
            'device_id' => 'nullable|string',
            'device_name' => 'nullable|string',
            'os_version' => 'nullable|string',
            'ip_address' => 'nullable|ip',
            'network_type' => 'nullable|string',

            // Raw payload
            'raw_payload' => 'nullable|array',
        ]);

        // Set default message type if not provided
        if (empty($validated['message_type'])) {
            $validated['message_type'] = 'text';
        }

        $notification = Notification::create($validated);

        return response()->json([
            'success' => true,
            'data' => $notification
        ], 201);
    }
}

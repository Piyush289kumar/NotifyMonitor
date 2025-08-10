<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Notification extends Model
{
    protected $fillable = [
        // App / Platform info
        'app_name',
        'app_identifier',
        'app_version',

        // Sender info
        'sender_id',
        'sender_name',
        'sender_username',
        'sender_profile_url',
        'sender_verified',
        'sender_phone',
        'sender_email',

        // Receiver info
        'receiver_id',
        'receiver_name',
        'receiver_username',
        'receiver_phone',
        'receiver_email',

        // Notification content
        'title',
        'message',
        'message_type',

        // Media / Attachments
        'media_url',
        'media_type',
        'media_size',
        'media_duration',
        'media_metadata',

        // Call / Special event data
        'call_type',
        'call_duration',

        // Status & Delivery Info
        'is_read',
        'is_deleted',
        'delivered_at',
        'read_at',

        // Device & Network Info
        'device_id',
        'device_name',
        'os_version',
        'ip_address',
        'network_type',

        // Raw JSON payload
        'raw_payload',
    ];

    protected $casts = [
        'sender_verified' => 'boolean',
        'is_read' => 'boolean',
        'is_deleted' => 'boolean',
        'media_metadata' => 'array',
        'raw_payload' => 'array',
        'delivered_at' => 'datetime',
        'read_at' => 'datetime',
    ];
}

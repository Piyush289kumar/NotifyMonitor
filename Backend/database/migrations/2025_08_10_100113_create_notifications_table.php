<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('notifications', function (Blueprint $table) {
            $table->id();


            /** App / Platform info **/
            $table->string('app_name'); // whatsapp, facebook, instagram
            $table->string('app_identifier')->nullable(); // e.g. com.whatsapp
            $table->string('app_version')->nullable();

            /** Sender info **/
            $table->string('sender_id')->nullable(); // platform user ID
            $table->string('sender_name')->nullable(); // display name
            $table->string('sender_username')->nullable(); // @username
            $table->string('sender_profile_url')->nullable(); // profile picture URL
            $table->boolean('sender_verified')->default(false);
            $table->string('sender_phone')->nullable();
            $table->string('sender_email')->nullable();

            /** Receiver info **/
            $table->string('receiver_id')->nullable();
            $table->string('receiver_name')->nullable();
            $table->string('receiver_username')->nullable();
            $table->string('receiver_phone')->nullable();
            $table->string('receiver_email')->nullable();

            /** Notification content **/
            $table->string('title')->nullable(); // notification title
            $table->longText('message')->nullable(); // full message text
            $table->enum('message_type', [
                'text', 'image', 'video', 'audio', 'file', 'link', 'call', 'sticker', 'system'
            ])->default('text');

            /** Media / Attachments **/
            $table->string('media_url')->nullable();
            $table->string('media_type')->nullable(); // MIME type
            $table->unsignedBigInteger('media_size')->nullable(); // bytes
            $table->integer('media_duration')->nullable(); // seconds (for audio/video)
            $table->json('media_metadata')->nullable(); // dimensions, codec, etc.

            /** Call / Special event data **/
            $table->enum('call_type', ['incoming', 'outgoing', 'missed'])->nullable();
            $table->integer('call_duration')->nullable(); // seconds

            /** Status & Delivery Info **/
            $table->boolean('is_read')->default(false);
            $table->boolean('is_deleted')->default(false);
            $table->timestamp('delivered_at')->nullable();
            $table->timestamp('read_at')->nullable();

            /** Device & Network Info **/
            $table->string('device_id')->nullable();
            $table->string('device_name')->nullable();
            $table->string('os_version')->nullable();
            $table->string('ip_address')->nullable();
            $table->string('network_type')->nullable(); // wifi, 4G, 5G

            /** Raw JSON payload for future-proofing **/
            $table->json('raw_payload')->nullable();


            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('notifications');
    }
};

<?php
namespace App\Filament\Resources;
use App\Filament\Resources\NotificationResource\Pages;
use App\Filament\Resources\NotificationResource\RelationManagers;
use App\Models\Notification;
use Filament\Forms;
use Filament\Forms\Components\DateTimePicker;
use Filament\Forms\Components\Section;
use Filament\Forms\Components\Select;
use Filament\Forms\Components\Textarea;
use Filament\Forms\Components\TextInput;
use Filament\Forms\Components\Toggle;
use Filament\Forms\Form;
use Filament\Resources\Resource;
use Filament\Tables;
use Filament\Tables\Columns\BadgeColumn;
use Filament\Tables\Columns\IconColumn;
use Filament\Tables\Columns\ImageColumn;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Table;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\SoftDeletingScope;
class NotificationResource extends Resource
{
    protected static ?string $model = Notification::class;
    protected static ?string $navigationIcon = 'heroicon-o-bell';
    protected static ?string $navigationGroup = 'Social Media Notifications';
    protected static ?string $navigationLabel = 'Notifications';
    public static function form(Form $form): Form
    {
        return $form
            ->schema([
                Section::make('Notification Content')
                    ->schema([
                        TextInput::make('title'),
                        Textarea::make('message')->rows(4),
                        Select::make('message_type')
                            ->options([
                                'text' => 'Text',
                                'image' => 'Image',
                                'video' => 'Video',
                                'audio' => 'Audio',
                                'file' => 'File',
                                'link' => 'Link',
                                'call' => 'Call',
                                'sticker' => 'Sticker',
                                'system' => 'System',
                            ])->default('text')
                            ->required(),
                    ])->columns(2),
                Section::make('App Info')
                    ->schema([
                        TextInput::make('app_name')->required(),
                        TextInput::make('app_identifier'),
                        TextInput::make('app_version'),
                    ])->columns(3),
                Section::make('Sender Info')
                    ->schema([
                        TextInput::make('sender_id'),
                        TextInput::make('sender_name'),
                        TextInput::make('sender_username'),
                        TextInput::make('sender_profile_url'),
                        Toggle::make('sender_verified'),
                        TextInput::make('sender_phone'),
                        TextInput::make('sender_email'),
                    ])->columns(3),
                Section::make('Receiver Info')
                    ->schema([
                        TextInput::make('receiver_id'),
                        TextInput::make('receiver_name'),
                        TextInput::make('receiver_username'),
                        TextInput::make('receiver_phone'),
                        TextInput::make('receiver_email'),
                    ])->columns(3),
                Section::make('Media / Attachments')
                    ->schema([
                        TextInput::make('media_url'),
                        TextInput::make('media_type'),
                        TextInput::make('media_size'),
                        TextInput::make('media_duration'),
                        Textarea::make('media_metadata'),
                    ])->columns(3),
                Section::make('Call Info')
                    ->schema([
                        Select::make('call_type')
                            ->options([
                                'incoming' => 'Incoming',
                                'outgoing' => 'Outgoing',
                                'missed' => 'Missed',
                            ]),
                        TextInput::make('call_duration'),
                    ])->columns(2),
                Section::make('Status & Delivery')
                    ->schema([
                        Toggle::make('is_read'),
                        Toggle::make('is_deleted'),
                        DateTimePicker::make('delivered_at'),
                        DateTimePicker::make('read_at'),
                    ])->columns(4),
                Section::make('Device & Network')
                    ->schema([
                        TextInput::make('device_id'),
                        TextInput::make('device_name'),
                        TextInput::make('os_version'),
                        TextInput::make('ip_address'),
                        TextInput::make('network_type'),
                    ])->columns(3),
                Textarea::make('raw_payload')->rows(6),
            ]);
    }
    public static function table(Table $table): Table
    {
        return $table
            ->columns([
                TextColumn::make('app_name')->sortable()->searchable(),
                TextColumn::make('title')->searchable()->wrap(true),
                TextColumn::make('message')->sortable()->searchable()->wrap(true),
                BadgeColumn::make('message_type')
                    ->colors([
                        'primary' => 'text',
                        'success' => 'image',
                        'warning' => 'video',
                        'danger' => 'call',
                    ]),
                TextColumn::make('delivered_at')->dateTime()->sortable(),
                TextColumn::make('created_at')->dateTime()->sortable(),
                TextColumn::make('device_name')->sortable(),
            ])->defaultSort('created_at', 'desc')
            ->filters([
                Tables\Filters\SelectFilter::make('app_name')
                    ->options([
                        'whatsapp' => 'WhatsApp',
                        'facebook' => 'Facebook',
                        'instagram' => 'Instagram',
                    ]),
                Tables\Filters\SelectFilter::make('message_type')
                    ->options([
                        'text' => 'Text',
                        'image' => 'Image',
                        'video' => 'Video',
                        'audio' => 'Audio',
                        'file' => 'File',
                        'link' => 'Link',
                        'call' => 'Call',
                        'sticker' => 'Sticker',
                        'system' => 'System',
                    ]),
                Tables\Filters\TernaryFilter::make('is_read')->label('Read Status'),
            ])
            ->actions([
                Tables\Actions\ViewAction::make(),
                Tables\Actions\EditAction::make(),
                Tables\Actions\DeleteAction::make(),
            ])
            ->bulkActions([
                Tables\Actions\DeleteBulkAction::make(),
            ]);
    }
    public static function getRelations(): array
    {
        return [
            //
        ];
    }
    public static function getPages(): array
    {
        return [
            'index' => Pages\ListNotifications::route('/'),
            'create' => Pages\CreateNotification::route('/create'),
            'edit' => Pages\EditNotification::route('/{record}/edit'),
        ];
    }
}

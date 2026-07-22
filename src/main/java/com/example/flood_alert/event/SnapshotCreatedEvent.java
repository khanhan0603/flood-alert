package com.example.flood_alert.event;

import java.util.UUID;

public record SnapshotCreatedEvent(UUID snapshotId) {
}
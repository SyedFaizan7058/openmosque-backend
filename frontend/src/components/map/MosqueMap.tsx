import React, { useEffect, useRef } from 'react';
import type { Mosque } from '../../types';

interface MosqueMapProps {
  userLocation: { latitude: number; longitude: number };
  radiusKm: number;
  mosques: Mosque[];
  selectedMosque: Mosque | null;
  onSelectMosque: (mosque: Mosque) => void;
}

// Global declaration for Leaflet loaded via CDN
declare global {
  interface Window {
    L: any;
  }
}

export const MosqueMap: React.FC<MosqueMapProps> = ({
  userLocation,
  radiusKm,
  mosques,
  selectedMosque,
  onSelectMosque,
}) => {
  const mapContainerRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<any>(null);
  const markersLayerRef = useRef<any>(null);
  const radiusCircleRef = useRef<any>(null);

  // Initialize Leaflet map
  useEffect(() => {
    if (!mapContainerRef.current) return;
    if (typeof window === 'undefined' || !window.L) return;

    if (!mapInstanceRef.current) {
      const map = window.L.map(mapContainerRef.current, {
        center: [userLocation.latitude, userLocation.longitude],
        zoom: radiusKm <= 5 ? 14 : radiusKm <= 15 ? 12 : 10,
        zoomControl: true,
      });

      window.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
      }).addTo(map);

      // User location marker
      const userIcon = window.L.divIcon({
        className: 'custom-user-pin',
        html: '<div style="width:14px;height:14px;background:#2563EB;border-radius:50%;border:2px solid #fff;box-shadow:0 0 10px rgba(37,99,235,0.8);"></div>',
        iconSize: [14, 14],
        iconAnchor: [7, 7],
      });

      window.L.marker([userLocation.latitude, userLocation.longitude], { icon: userIcon })
        .addTo(map)
        .bindPopup('<b>Your Location</b><br/>GPS Center Point');

      mapInstanceRef.current = map;
      markersLayerRef.current = window.L.layerGroup().addTo(map);
    }

    return () => {
      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove();
        mapInstanceRef.current = null;
      }
    };
  }, []);

  // Update map center and radius circle when user location or radius changes
  useEffect(() => {
    if (!mapInstanceRef.current || !window.L) return;

    mapInstanceRef.current.setView(
      [userLocation.latitude, userLocation.longitude],
      radiusKm <= 5 ? 14 : radiusKm <= 15 ? 12 : 10
    );

    if (radiusCircleRef.current) {
      mapInstanceRef.current.removeLayer(radiusCircleRef.current);
    }

    radiusCircleRef.current = window.L.circle([userLocation.latitude, userLocation.longitude], {
      radius: radiusKm * 1000,
      color: '#059669',
      fillColor: '#10b981',
      fillOpacity: 0.08,
      weight: 1.5,
      dashArray: '4, 4',
    }).addTo(mapInstanceRef.current);
  }, [userLocation.latitude, userLocation.longitude, radiusKm]);

  // Update mosque markers
  useEffect(() => {
    if (!mapInstanceRef.current || !markersLayerRef.current || !window.L) return;

    markersLayerRef.current.clearLayers();

    mosques.forEach((mosque) => {
      const isSelected = selectedMosque?.id === mosque.id;

      const mosqueIcon = window.L.divIcon({
        className: 'mosque-marker-pin',
        html: `
          <div style="
            background: ${isSelected ? '#047857' : '#059669'};
            color: #ffffff;
            width: ${isSelected ? '38px' : '32px'};
            height: ${isSelected ? '38px' : '32px'};
            border-radius: 50% 50% 50% 0;
            transform: rotate(-45deg);
            display: flex;
            align-items: center;
            justify-content: center;
            border: 2.5px solid #ffffff;
            box-shadow: 0 4px 10px rgba(0,0,0,0.3);
            cursor: pointer;
            transition: all 0.2s ease;
          ">
            <span style="transform: rotate(45deg); font-size: ${isSelected ? '15px' : '13px'}; font-weight: bold;">🕌</span>
          </div>
        `,
        iconSize: isSelected ? [38, 38] : [32, 32],
        iconAnchor: isSelected ? [19, 38] : [16, 32],
        popupAnchor: [0, -32],
      });

      const marker = window.L.marker([mosque.latitude, mosque.longitude], { icon: mosqueIcon });

      const popupContent = `
        <div style="min-width: 180px; font-family: sans-serif;">
          <h4 style="margin: 0 0 4px 0; font-size: 14px; font-weight: bold; color: #0f172a;">${mosque.name}</h4>
          <p style="margin: 0 0 6px 0; font-size: 12px; color: #64748b;">${mosque.address}, ${mosque.city}</p>
          ${mosque.distanceKm !== undefined ? `<span style="font-size: 11px; background: #ecfdf5; color: #047857; padding: 2px 6px; border-radius: 4px; font-weight: 600;">${mosque.distanceKm.toFixed(1)} km away</span>` : ''}
        </div>
      `;

      marker.bindPopup(popupContent);
      marker.on('click', () => {
        onSelectMosque(mosque);
      });

      markersLayerRef.current.addLayer(marker);
    });
  }, [mosques, selectedMosque, onSelectMosque]);

  // Pan to selected mosque
  useEffect(() => {
    if (selectedMosque && mapInstanceRef.current) {
      mapInstanceRef.current.panTo([selectedMosque.latitude, selectedMosque.longitude], {
        animate: true,
        duration: 0.6,
      });
    }
  }, [selectedMosque]);

  return (
    <div className="relative w-full h-full rounded-2xl overflow-hidden border border-slate-200 shadow-sm bg-slate-100">
      <div ref={mapContainerRef} className="w-full h-full min-h-[400px]" />

      {/* Map Legend Overlay */}
      <div className="absolute top-3 right-3 z-[1000] bg-white/95 backdrop-blur-md px-3 py-2 rounded-xl border border-slate-200 shadow-md text-xs space-y-1">
        <div className="flex items-center gap-2">
          <span className="w-3 h-3 rounded-full bg-emerald-600 inline-block border border-white"></span>
          <span className="text-slate-700 font-medium">Mosque Marker</span>
        </div>
        <div className="flex items-center gap-2">
          <span className="w-3 h-3 rounded-full bg-blue-600 inline-block border border-white"></span>
          <span className="text-slate-700 font-medium">Your GPS Center</span>
        </div>
        <div className="text-[10px] text-slate-500 pt-0.5 border-t border-slate-100 font-semibold">
          Radius: {radiusKm} km ({mosques.length} found)
        </div>
      </div>
    </div>
  );
};

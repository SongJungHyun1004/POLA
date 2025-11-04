"use client";

import { useState, useEffect } from "react";
import {
  DndContext,
  closestCenter,
  PointerSensor,
  useSensor,
  useSensors,
} from "@dnd-kit/core";
import {
  arrayMove,
  SortableContext,
  useSortable,
  rectSortingStrategy,
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import PolaroidCard from "@/app/home/components/PolaroidCard";
import { Plus, Pencil } from "lucide-react";
import PolaroidDetail from "../categories/[id]/components/PolaroidDetail";

type SortableItemProps = {
  img: {
    id: number;
    src: string;
    tags: string[];
    contexts: string;
    favorite: boolean;
    date: string;
  };
  rotation: string;
  selected: number | null;
  onSelect: (id: number) => void;
};

function SortableItem({
  img,
  rotation,
  selected,
  onSelect,
}: SortableItemProps) {
  const { attributes, listeners, setNodeRef, transform, transition } =
    useSortable({ id: img.id });

  const style = {
    transform: CSS.Transform.toString(transform) || rotation,
    transition: transition || "transform 0.2s ease",
    transformOrigin: "center bottom",
  };

  return (
    <div ref={setNodeRef} style={style} className="w-fit overflow-visible">
      <button
        {...attributes}
        {...listeners}
        onClick={() => onSelect(img.id)}
        className={`relative hover:scale-[1.08] transition-transform ${
          selected === img.id ? "opacity-90" : "opacity-100"
        }`}
      >
        <PolaroidCard src={img.src} />
        {img.favorite && (
          <span className="absolute top-2 right-2 text-yellow-500 text-lg">
            ★
          </span>
        )}
      </button>
    </div>
  );
}

export default function FavoritePage() {
  const [selected, setSelected] = useState<number | null>(null);

  const [images, setImages] = useState(() =>
    Array.from({ length: 30 }, (_, i) => ({
      id: i + 1,
      src: "/images/dummy_image_1.png",
      tags: ["#태그1", "#태그2", "#태그3", "#태그4", "#태그5", "#태그6"],
      contexts: "내용을 입력하세요...",
      favorite: true,
      date: "2025.10.30",
    }))
  );

  const [rotations, setRotations] = useState<string[]>([]);
  const selectedImage = images.find((img) => img.id === selected);

  useEffect(() => {
    const newRotations = Array.from({ length: images.length }, () => {
      const deg = Math.random() * 8 - 4;
      return `rotate(${deg}deg)`;
    });
    setRotations(newRotations);
  }, [images.length]);

  // long press sensor
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: { delay: 200, tolerance: 5 },
    })
  );

  const handleDragEnd = (event: any) => {
    const { active, over } = event;
    if (!over || active.id === over.id) return;

    const oldIndex = images.findIndex((img) => img.id === active.id);
    const newIndex = images.findIndex((img) => img.id === over.id);

    // 임시 상태 변경 (API 연동되면 이 부분에서 PATCH)
    setImages((prev) => arrayMove(prev, oldIndex, newIndex));
  };

  return (
    <div className="flex h-full bg-[#FFFEF8] text-[#4C3D25] px-8 py-6 gap-8">
      {/* 좌측 메인 */}
      <div className="flex flex-col flex-1 overflow-hidden">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-6xl font-bold mb-2">Favorite</h1>
          <div className="flex items-center gap-4">
            <button className="p-2 rounded-full hover:bg-[#EDE6D8]">
              <Plus className="w-5 h-5" />
            </button>
            <button className="p-2 rounded-full hover:bg-[#EDE6D8]">
              <Pencil className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* DnD 리스트 */}
        <div className="flex-1 overflow-y-auto pr-2">
          <DndContext
            sensors={sensors}
            collisionDetection={closestCenter}
            onDragEnd={handleDragEnd}
          >
            <SortableContext items={images} strategy={rectSortingStrategy}>
              <div className="grid grid-cols-6 gap-6 overflow-visible p-6">
                {images.map((img, i) => (
                  <SortableItem
                    key={img.id}
                    img={img}
                    rotation={rotations[i]}
                    selected={selected}
                    onSelect={setSelected}
                  />
                ))}
              </div>
            </SortableContext>
          </DndContext>
        </div>
      </div>

      {/* 우측 상세 */}
      <div className="w-2/7 flex-shrink-0 border-l border-[#E3DCC8] pl-6 flex flex-col items-center justify-center">
        <PolaroidDetail
          id={selectedImage?.id}
          src={selectedImage?.src}
          tags={selectedImage?.tags ?? []}
          contexts={selectedImage?.contexts ?? ""}
          date={selectedImage?.date}
        />
      </div>
    </div>
  );
}

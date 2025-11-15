"use client";

import { useSearchParams } from "next/navigation";
import { useState, useRef, useEffect } from "react";
import PolaroidDetail from "../categories/[id]/components/PolaroidDetail";
import PolaroidCard from "../home/components/PolaroidCard";
import { Send } from "lucide-react";
import { ragSearch } from "@/services/ragService";

interface ChatMessage {
  role: "user" | "assistant";
  content: string;
}

// 카드 그룹
interface CardGroup {
  answerIndex: number;
  cards: {
    id: number;
    src: string;
    context: string;
    type: string;
    platform?: string;
    ocr_text: string;
    favorite: boolean;
    tags: string[];
    rotate: number;
    createdAt: string;
  }[];
}

export default function RAGSearchPageInner() {
  const params = useSearchParams();
  const initialQuery = params.get("query") ?? "";

  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [cardGroups, setCardGroups] = useState<CardGroup[]>([]);
  const [query, setQuery] = useState("");
  const [detail, setDetail] = useState<any | null>(null);
  const [loading, setLoading] = useState(false);
  const [layoutExpanded, setLayoutExpanded] = useState(false);

  const scrollRef = useRef<HTMLDivElement>(null);
  const hasRun = useRef(false);

  useEffect(() => {
    if (detail && !layoutExpanded) setLayoutExpanded(true);
  }, [detail]);

  useEffect(() => {
    scrollRef.current?.scrollTo({
      top: scrollRef.current.scrollHeight,
      behavior: "smooth",
    });
  }, [messages, cardGroups]);

  // 초기 자동 검색
  useEffect(() => {
    if (hasRun.current) return;
    hasRun.current = true;

    if (!initialQuery) return;

    setMessages([{ role: "user", content: initialQuery }]);
    performSearch(initialQuery);
  }, [initialQuery]);

  /** 검색 실행 */
  const performSearch = async (text: string) => {
    setLoading(true);

    try {
      const response = await ragSearch(text);

      // ★ API 구조 변경 반영
      const answer = response.data.answer;
      const sources = response.data.sources;

      // AI 답변 추가
      setMessages((prev) => [...prev, { role: "assistant", content: answer }]);

      const answerIndex = messages.length;

      // 카드 그룹 생성
      const newCards = (sources || []).map((s: any) => ({
        id: s.id,
        src: s.src,
        context: s.context,
        type: s.type,
        platform: s.platform,
        ocr_text: s.ocr_text,
        favorite: s.favorite,
        tags: s.tags || [],
        rotate: Math.random() * 8 - 4,
        createdAt: s.createdAt,
      }));

      setCardGroups((prev) => [...prev, { answerIndex, cards: newCards }]);

      // detail 설정
      if (newCards.length > 0) {
        const c = newCards[0];
        setDetail({
          id: c.id,
          src: c.src,
          tags: c.tags.map((t: string) => `#${t}`),
          contexts: c.context,
          favorite: c.favorite,
          type: c.type,
          platform: c.platform,
          ocr_text: c.ocr_text,
          date: c.createdAt,
        });
      }
    } catch (err) {
      console.error(err);
      setMessages((prev) => [
        ...prev,
        { role: "assistant", content: "AI 검색 중 오류가 발생했습니다." },
      ]);
    } finally {
      setLoading(false);
    }
  };

  /** Detail에서 favorite 변경 → 카드/상세同步 */
  const handleFavoriteChange = (newState: boolean) => {
    if (!detail) return;

    // detail 업데이트
    setDetail((prev: any) => ({ ...prev, favorite: newState }));

    // 카드 리스트에도 반영
    setCardGroups((prev) =>
      prev.map((group) => ({
        ...group,
        cards: group.cards.map((c) =>
          c.id === detail.id ? { ...c, favorite: newState } : c
        ),
      }))
    );
  };

  /** 입력 전송 */
  const handleSend = () => {
    const text = query.trim();
    if (!text) return;

    setMessages((prev) => [...prev, { role: "user", content: text }]);
    setQuery("");
    performSearch(text);
  };

  return (
    <div className="w-full h-[calc(100vh-100px)] flex justify-center bg-[#FFFEF8] text-[#4C3D25] overflow-hidden">
      <div
        className="h-full flex flex-row gap-6 pb-6 pl-6 transition-all duration-500"
        style={{ width: layoutExpanded ? "1200px" : "720px" }}
      >
        {/* LEFT AREA */}
        <div
          className="flex-1 h-full flex flex-col transition-all duration-500"
          style={{ width: "720px" }}
        >
          <div className="flex flex-col bg-[#F4EFE2] rounded-2xl shadow-sm flex-1 overflow-hidden">
            <div
              ref={scrollRef}
              className="flex-1 overflow-y-auto p-6 pr-2 scrollbar-thin scrollbar-thumb-[#CBBF9E]/50"
            >
              {/* 메시지 + 카드 그룹 */}
              {messages.map((msg, i) => (
                <div key={i} className="mb-10">
                  {/* 메시지 */}
                  <div
                    className={`flex mb-4 ${
                      msg.role === "user" ? "justify-end" : "justify-start"
                    }`}
                  >
                    {msg.role === "assistant" && (
                      <div className="w-10 h-10 rounded-full overflow-hidden mr-3">
                        <img
                          src="/images/POLA_chatbot.png"
                          className="w-full h-full object-cover"
                        />
                      </div>
                    )}

                    <div
                      className={`max-w-[70%] p-4 rounded-2xl text-base shadow-sm leading-relaxed ${
                        msg.role === "user"
                          ? "bg-[#4C3D25] text-white"
                          : "bg-white text-[#4C3D25] border border-[#E3DCC8]"
                      }`}
                    >
                      {msg.content}
                    </div>
                  </div>

                  {/* 카드 그룹 */}
                  {cardGroups
                    .filter((g) => g.answerIndex === i)
                    .map((group, gi) => (
                      <div
                        key={gi}
                        className="grid grid-cols-2 gap-4 ml-14 mt-3 w-[330px]"
                      >
                        {group.cards.map((c, idx) => (
                          <div
                            key={idx}
                            onClick={() =>
                              setDetail({
                                id: c.id,
                                src: c.src,
                                tags: c.tags.map((t: string) => `#${t}`),
                                contexts: c.context,
                                favorite: c.favorite,
                                type: c.type,
                                platform: c.platform,
                                ocr_text: c.ocr_text,
                                date: c.createdAt,
                              })
                            }
                            className="relative cursor-pointer transition-transform duration-200 hover:scale-105"
                            style={{ transform: `rotate(${c.rotate}deg)` }}
                          >
                            {/* Favorite 별 표시 */}
                            {c.favorite && (
                              <span className="absolute top-2 right-6 text-yellow-500 text-lg z-50">
                                ★
                              </span>
                            )}

                            <PolaroidCard
                              src={c.src}
                              type={c.type}
                              ocr_text={c.ocr_text}
                            />
                          </div>
                        ))}
                      </div>
                    ))}
                </div>
              ))}

              {loading && (
                <div className="text-center text-sm text-[#7A6A48] mt-2">
                  AI가 답변을 생성하는 중...
                </div>
              )}
            </div>

            {/* 입력창 */}
            <div className="p-4 flex items-center gap-2 border-t border-[#E5DECF] bg-[#F4EFE2]">
              <input
                type="text"
                placeholder="AI에게 질문해보세요..."
                className="flex-grow bg-white border border-[#D8D5CC] rounded-full px-4 py-3 outline-none"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleSend()}
              />

              <button
                onClick={handleSend}
                className="w-12 h-12 flex items-center justify-center rounded-full bg-[#4C3D25] text-white hover:bg-[#3A311F]"
              >
                <Send className="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>

        {/* RIGHT DETAIL */}
        <div
          className="flex-shrink-0 border-l border-[#E3DCC8] flex justify-center pt-8 overflow-y-auto pl-4 transition-all duration-500"
          style={{
            width: layoutExpanded ? "400px" : "0px",
            opacity: layoutExpanded ? 1 : 0,
            pointerEvents: layoutExpanded ? "auto" : "none",
          }}
        >
          {detail && (
            <PolaroidDetail
              id={detail.id}
              src={detail.src}
              tags={detail.tags}
              contexts={detail.contexts}
              date={detail.date}
              favorite={detail.favorite}
              type={detail.type}
              platform={detail.platform}
              ocr_text={detail.ocr_text}
              onFavoriteChange={handleFavoriteChange}
            />
          )}
        </div>
      </div>
    </div>
  );
}

import CategoryBox from "./components/CategoryBox";
import Timeline from "./components/Timeline";
import CategoryRow from "./components/CategoryRow";
import TextLink from "./components/TextLink";

export default function HomePage() {
  return (
    <main className="h-full text-[#4C3D25] px-8 pb-12 bg-[#FFFEF8]">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 h-full">
        {/* 좌측 영역 */}
        <div className="flex flex-col items-center justify-start gap-12 w-full mt-16">
          <div className="flex justify-center gap-10 w-full">
            <CategoryBox text="Favorite" />
            <div className="flex w-8" />
            <CategoryBox text="Remind" />
          </div>

          <div className="flex flex-col items-center justify-center w-full mt-8">
            <Timeline />
            <TextLink text="Timeline" link="/timeline" />
          </div>
        </div>

        {/* 우측 영역 */}
        <div className="flex flex-col gap-8 overflow-y-auto pr-2 w-full h-full">
          {[
            { id: 1, name: "Travel" },
            { id: 2, name: "Food" },
            { id: 3, name: "Daily" },
            { id: 4, name: "Friends" },
            { id: 5, name: "Memories" },
          ].map((category) => {
            const imgSrc = "/images/POLA_logo_1.png";

            return (
              <div key={category.id} className="w-full flex-shrink-0">
                <TextLink
                  text={category.name}
                  link={"/category/" + category.id}
                />
                <CategoryRow imgSrc={imgSrc} />
              </div>
            );
          })}
        </div>
      </div>
    </main>
  );
}

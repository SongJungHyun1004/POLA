interface CategoryPageProps {
  params: Promise<{ id: string }>;
}

export default async function CategoryPage({ params }: CategoryPageProps) {
  const { id } = await params;

  return (
    <div className="flex flex-col items-center justify-center min-h-screen">
      <h1 className="text-3xl font-bold mb-4">카테고리 페이지</h1>
      <p className="text-lg">
        현재 카테고리 ID: <strong>{id}</strong>
      </p>
    </div>
  );
}

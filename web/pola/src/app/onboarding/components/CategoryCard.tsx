interface CategoryCardProps {
  category: string;
  onClick: () => void;
  className?: string;
}

const CategoryCard: React.FC<CategoryCardProps> = ({
  category,
  onClick,
  className,
}) => {
  return (
    <div
      className={`flex items-center justify-center bg-[#FEF5DA] text-[#B0804C] rounded-md p-4 m-2 cursor-pointer hover:bg-[#ffe493] hover:text-[#b36b1e] ${className}`}
      onClick={onClick}
    >
      <span className="text-lg font-semibold">{category}</span>
    </div>
  );
};

export default CategoryCard;

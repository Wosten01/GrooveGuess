import { Button, ButtonProps } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { ReactNode } from "react";
import { useAuth } from "../../../hooks/auth-context";

type RoleBasedNavButtonProps = {
  allowedRoles: string[];
  to: string;            
  label: ReactNode;   
  icon?: ReactNode;     
  buttonProps?: ButtonProps;
};

export const NavButton: React.FC<RoleBasedNavButtonProps> = ({
  allowedRoles,
  to,
  label,
  icon,
  buttonProps,
}) => {
  const { user } = useAuth();
  const navigate = useNavigate();

  if (!user || !allowedRoles.includes(user.role)) return null;

  return (
    <Button
      color="primary"
      variant="outlined"
      startIcon={icon}
      onClick={() => navigate(to)}
      sx={{ ml: 2 }}
      {...buttonProps}
    >
      {label}
    </Button>
  );
};
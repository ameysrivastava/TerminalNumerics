package net.anasa.math.interpreter.sequence;

import net.anasa.math.MathNumber;
import net.anasa.math.expression.ConstantType;
import net.anasa.math.expression.FunctionExpression;
import net.anasa.math.expression.FunctionType;
import net.anasa.math.expression.IExpression;
import net.anasa.math.expression.IFunction;
import net.anasa.math.expression.NumberExpression;
import net.anasa.math.expression.OperationExpression;
import net.anasa.math.expression.OperatorType;
import net.anasa.math.expression.VariableExpression;
import net.anasa.math.sequence.SequenceNesting;
import net.anasa.math.sequence.TokenType;
import net.anasa.util.Checks;
import net.anasa.util.Listing;
import net.anasa.util.NumberHelper;
import net.anasa.util.StringHelper;
import net.anasa.util.StringHelper.NestingException;
import net.anasa.util.data.resolver.IToken;
import net.anasa.util.data.resolver.MultiResolver;
import net.anasa.util.data.resolver.ResolverException;
import net.anasa.util.data.resolver.Token;
import net.anasa.util.data.resolver.logic.CollectionResolver;
import net.anasa.util.data.resolver.logic.ComplexResolver;
import net.anasa.util.data.resolver.logic.IResolver;

public class ExpressionResolver extends MultiResolver<IExpression>
{
	public ExpressionResolver()
	{
		ExpressionResolver expression = this;
		
		add(new ComplexResolver<IExpression>("multiply")
		{
			Consumer<Listing<IToken>> a = new Consumer<>(new CollectionResolver<>(expression));
			Consumer<Listing<IToken>> b = new Consumer<>(new CollectionResolver<>(expression));
			
			@Override
			public IExpression resolve(ConsumerStorage storage) throws ResolverException
			{
				return expression.resolve(new Listing<>(storage.get(a)).add(new Token(TokenType.OPERATOR.name(), "*")).addAll(storage.get(b)));
			}
		});
		
		add(new ITypeResolver<IExpression>()
		{
			@Override
			public TokenType getType()
			{
				return TokenType.NUMBER;
			}
			
			@Override
			public IExpression resolve(IToken item) throws ResolverException
			{
				String data = item.getData();
				
				ConstantType constant = ConstantType.get(data);
				if(constant != null)
				{
					return new NumberExpression(new MathNumber(constant.getValue()));
				}
				
				Checks.check(NumberHelper.isDouble(data), new ResolverException("Invalid number: " + item.getData()));
				return new NumberExpression(new MathNumber(NumberHelper.getDouble(data)));
			}
		});
		
		add(new ITypeResolver<IExpression>()
		{
			@Override
			public TokenType getType()
			{
				return TokenType.VARIABLE;
			}
			
			@Override
			public IExpression resolve(IToken item) throws ResolverException
			{
				return new VariableExpression(item.getData());
			}
		});
		
		add(new ComplexResolver<IExpression>("parenthesis")
		{
			Consumer<IExpression> inner;
			
			{
				new Consumer<>(new TypeResolver(TokenType.OPEN_PARENTHESIS));
				inner = new Consumer<>(expression);
				new Consumer<>(new TypeResolver(TokenType.CLOSE_PARENTHESIS));
			}
			
			@Override
			public IExpression resolve(ConsumerStorage storage) throws ResolverException
			{
				return storage.get(inner);
			}
		});
		
		add(new IResolver<IExpression>()
		{
			@Override
			public boolean matches(Listing<IToken> data)
			{
				try
				{
					resolve(data);
					return true;
				}
				catch(ResolverException e)
				{
					return false;
				}
			}
			
			@Override
			public IExpression resolve(Listing<IToken> data) throws ResolverException
			{
				try
				{
					Checks.check(SequenceNesting.isNestingValid(data) && SequenceNesting.stripNesting(data).contains((item) -> TokenType.OPERATOR.isType(item.getType())),
							new ResolverException("Invalid operator input data: " + data));
					
					IToken splitter = null;
					
					for(IToken item : SequenceNesting.stripNesting(data))
					{
						if(TokenType.OPERATOR.isType(item.getType()))
						{
							OperatorType op = OperatorType.get(item.getData());
							
							if(splitter == null || !op.hasPriority(OperatorType.get(splitter.getData())))
							{
								splitter = item;
							}
						}
					}
					
					Checks.checkNotNull(splitter, new ResolverException("Sequence must contain an operator"));
					
					int index = data.indexOf(splitter);
					
					IExpression a = expression.resolve(data.subList(0, index));
					IExpression b = expression.resolve(data.subList(index + 1));
					
					return new OperationExpression(OperatorType.get(splitter.getData()), a, b);
				}
				catch(NestingException e)
				{
					throw new ResolverException(e);
				}
			}
		});
		
		add(new ComplexResolver<IExpression>("function")
		{
			Consumer<IFunction> function = new Consumer<>(new ITypeResolver<IFunction>()
			{
				@Override
				public TokenType getType()
				{
					return TokenType.FUNCTION;
				}
				
				@Override
				public IFunction resolve(IToken item) throws ResolverException
				{
					return FunctionType.get(item.getData());
				}
			});
			
			Consumer<IExpression> operand = new Consumer<>(expression);
			
			@Override
			public IExpression resolve(ConsumerStorage storage) throws ResolverException
			{
				return new FunctionExpression(storage.get(function), storage.get(operand));
			}
		});
		
		add(new ComplexResolver<IExpression>("negative")
		{
			Consumer<OperatorType> negative = new Consumer<>(new ITypeResolver<OperatorType>()
			{
				@Override
				public TokenType getType()
				{
					return TokenType.OPERATOR;
				}
				
				@Override
				public boolean matches(IToken item)
				{
					return StringHelper.equals(OperatorType.SUBTRACT.getSignature(), item.getData());
				}
				
				@Override
				public OperatorType resolve(IToken item) throws ResolverException
				{
					return OperatorType.SUBTRACT;
				}
			});
			
			Consumer<IExpression> operand = new Consumer<>(expression);
			
			@Override
			public IExpression resolve(ConsumerStorage storage) throws ResolverException
			{
				return new OperationExpression(storage.get(negative), new NumberExpression(new MathNumber(0)), storage.get(operand));
			}
		});
	}
}
